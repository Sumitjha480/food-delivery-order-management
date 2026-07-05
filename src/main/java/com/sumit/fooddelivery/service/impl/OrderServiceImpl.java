package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.OrderItemRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.response.OrderItemResponse;
import com.sumit.fooddelivery.dto.response.OrderResponse;
import com.sumit.fooddelivery.entity.*;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentStatus;
import com.sumit.fooddelivery.payment.PaymentResult;
import com.sumit.fooddelivery.repository.*;
import com.sumit.fooddelivery.security.CurrentUserService;
import com.sumit.fooddelivery.service.OrderService;
import com.sumit.fooddelivery.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.sumit.fooddelivery.event.DeliveryPartnerAssignedEvent;
import com.sumit.fooddelivery.event.OrderStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserService currentUserService;


    @Override
    public OrderResponse create(OrderRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        currentUserService.requireAdminOrCustomer(customer);

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryPartner(null);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPayment().getMethod());

        List<OrderItemRequest> mergedItems = mergeDuplicateMenuItems(request.getItems());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : mergedItems) {

            MenuItem menuItem = menuItemRepository.findByIdForUpdate(itemReq.getMenuItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Menu item not found"));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException(
                        "Menu item " + menuItem.getId() + " does not belong to restaurant " + restaurant.getId()
                );
            }

            if (menuItem.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for item: " + menuItem.getName()
                                + ". Available stock: " + menuItem.getStock()
                                + ", requested quantity: " + itemReq.getQuantity()
                );
            }

            menuItem.setStock(menuItem.getStock() - itemReq.getQuantity());
            menuItemRepository.save(menuItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            total = total.add(menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);

        PaymentResult paymentResult = paymentService.charge(total, request.getPayment());

        if (!paymentResult.isSuccess()) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentFailureReason(paymentResult.getFailureReason());

            throw new IllegalArgumentException(
                    "Payment failed: " + paymentResult.getFailureReason()
            );
        }

        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentReference(paymentResult.getReference());
        order.setPaidAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        publishOrderStatusChanged(savedOrder, null);

        return mapToResponse(savedOrder, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(order -> mapToResponse(order, order.getOrderItems()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {

        Order order = getOrderOrThrow(id);

        return mapToResponse(order, order.getOrderItems());
    }

    @Override
    public OrderResponse accept(Long id) {

        Order order = getOrderOrThrow(id);
        currentUserService.requireAdminOrRestaurantOwner(order.getRestaurant());

        OrderStatus oldStatus = order.getOrderStatus();

        validateCurrentStatus(order, OrderStatus.PLACED, "Only PLACED orders can be accepted");

        if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successfully paid orders can be accepted");
        }

        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusChanged(savedOrder, oldStatus);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse reject(Long id, String reason) {

        Order order = getOrderOrThrow(id);
        currentUserService.requireAdminOrRestaurantOwner(order.getRestaurant());

        OrderStatus oldStatus = order.getOrderStatus();

        validateCurrentStatus(order, OrderStatus.PLACED, "Only PLACED orders can be rejected");

        restoreStock(order);

        order.setOrderStatus(OrderStatus.REJECTED);
        order.setRejectedAt(LocalDateTime.now());
        order.setRejectionReason(reason != null && !reason.isBlank()
                ? reason
                : "Rejected by restaurant");

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setRefundedAt(LocalDateTime.now());
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentFailureReason("Order rejected before successful payment");
        }

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusChanged(savedOrder, oldStatus);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {

        Order order = getOrderForUpdateOrThrow(orderId);
        currentUserService.requireAdminOrRestaurantOwner(order.getRestaurant());
        DeliveryPartner deliveryPartner = getDeliveryPartnerForUpdateOrThrow(deliveryPartnerId);

        validateOrderCanReceivePartner(order);
        validatePartnerAvailable(deliveryPartner);
        validatePartnerServicesRestaurantCity(order, deliveryPartner);

        order.setDeliveryPartner(deliveryPartner);
        deliveryPartner.setStatus(DeliveryPartnerStatus.BUSY);

        deliveryPartnerRepository.save(deliveryPartner);
        Order savedOrder = orderRepository.save(order);

        publishDeliveryPartnerAssigned(savedOrder, deliveryPartner);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse claimDeliveryPartner(Long orderId, Long deliveryPartnerId) {

        Order order = getOrderForUpdateOrThrow(orderId);
        DeliveryPartner deliveryPartner = getDeliveryPartnerForUpdateOrThrow(deliveryPartnerId);
        currentUserService.requireAdminOrDeliveryPartner(deliveryPartner);

        validateOrderCanReceivePartner(order);
        validatePartnerAvailable(deliveryPartner);
        validatePartnerServicesRestaurantCity(order, deliveryPartner);

        order.setDeliveryPartner(deliveryPartner);
        deliveryPartner.setStatus(DeliveryPartnerStatus.BUSY);

        deliveryPartnerRepository.save(deliveryPartner);
        Order savedOrder = orderRepository.save(order);

        publishDeliveryPartnerAssigned(savedOrder, deliveryPartner);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markPreparing(Long id) {

        Order order = getOrderOrThrow(id);
        currentUserService.requireAdminOrRestaurantOwner(order.getRestaurant());

        OrderStatus oldStatus = order.getOrderStatus();

        validateCurrentStatus(order, OrderStatus.ACCEPTED, "Only ACCEPTED orders can move to PREPARING");

        order.setOrderStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusChanged(savedOrder, oldStatus);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markOutForDelivery(Long id) {

        Order order = getOrderOrThrow(id);

        OrderStatus oldStatus = order.getOrderStatus();

        validateCurrentStatus(order, OrderStatus.PREPARING, "Only PREPARING orders can move to OUT_FOR_DELIVERY");

        if (order.getDeliveryPartner() == null) {
            throw new IllegalArgumentException("Cannot mark order out for delivery without assigned delivery partner");
        }
        currentUserService.requireAdminOrDeliveryPartner(order.getDeliveryPartner());

        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setOutForDeliveryAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusChanged(savedOrder, oldStatus);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markDelivered(Long id) {

        Order order = getOrderOrThrow(id);

        OrderStatus oldStatus = order.getOrderStatus();

        validateCurrentStatus(order, OrderStatus.OUT_FOR_DELIVERY, "Only OUT_FOR_DELIVERY orders can be delivered");

        if (order.getDeliveryPartner() == null) {
            throw new IllegalArgumentException("Cannot deliver order without assigned delivery partner");
        }

        currentUserService.requireAdminOrDeliveryPartner(order.getDeliveryPartner());

        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());

        order.getDeliveryPartner().setStatus(DeliveryPartnerStatus.AVAILABLE);
        deliveryPartnerRepository.save(order.getDeliveryPartner());

        Order savedOrder = orderRepository.save(order);

        publishOrderStatusChanged(savedOrder, oldStatus);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }


    @Override
    public void delete(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found");
        }

        orderRepository.deleteById(id);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    private Order getOrderForUpdateOrThrow(Long id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    private DeliveryPartner getDeliveryPartnerForUpdateOrThrow(Long id) {
        return deliveryPartnerRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery partner not found"));
    }

    private void validateCurrentStatus(Order order, OrderStatus expectedStatus, String message) {

        if (order.getOrderStatus() != expectedStatus) {
            throw new IllegalArgumentException(
                    message + ". Current status is " + order.getOrderStatus()
            );
        }
    }

    private void validateOrderCanReceivePartner(Order order) {

        if (order.getDeliveryPartner() != null) {
            throw new IllegalArgumentException("Order already has an assigned delivery partner");
        }

        if (order.getOrderStatus() != OrderStatus.ACCEPTED
                && order.getOrderStatus() != OrderStatus.PREPARING) {
            throw new IllegalArgumentException(
                    "Delivery partner can be assigned only after order is ACCEPTED or PREPARING. Current status is "
                            + order.getOrderStatus()
            );
        }
    }

    private void validatePartnerAvailable(DeliveryPartner deliveryPartner) {

        if (deliveryPartner.getStatus() != DeliveryPartnerStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Delivery partner is not available. Current status is " + deliveryPartner.getStatus()
            );
        }
    }

    private void restoreStock(Order order) {

        for (OrderItem orderItem : order.getOrderItems()) {
            MenuItem menuItem = orderItem.getMenuItem();
            menuItem.setStock(menuItem.getStock() + orderItem.getQuantity());
            menuItemRepository.save(menuItem);
        }
    }

    private List<OrderItemRequest> mergeDuplicateMenuItems(List<OrderItemRequest> items) {

        Map<Long, Integer> quantityByMenuItemId = new LinkedHashMap<>();

        for (OrderItemRequest item : items) {
            quantityByMenuItemId.merge(
                    item.getMenuItemId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        return quantityByMenuItemId.entrySet()
                .stream()
                .map(entry -> {
                    OrderItemRequest request = new OrderItemRequest();
                    request.setMenuItemId(entry.getKey());
                    request.setQuantity(entry.getValue());
                    return request;
                })
                .toList();
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {

        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> OrderItemResponse.builder()
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomer().getName())
                .restaurantName(order.getRestaurant().getName())
                .deliveryPartnerName(order.getDeliveryPartner() != null
                        ? order.getDeliveryPartner().getName()
                        : null)
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentReference(order.getPaymentReference())
                .paymentFailureReason(order.getPaymentFailureReason())
                .paidAt(order.getPaidAt())
                .refundedAt(order.getRefundedAt())
                .acceptedAt(order.getAcceptedAt())
                .preparingAt(order.getPreparingAt())
                .outForDeliveryAt(order.getOutForDeliveryAt())
                .deliveredAt(order.getDeliveredAt())
                .rejectedAt(order.getRejectedAt())
                .rejectionReason(order.getRejectionReason())
                .items(itemResponses)
                .build();
    }

    private void publishOrderStatusChanged(Order order, OrderStatus oldStatus) {
        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(
                        order.getId(),
                        oldStatus,
                        order.getOrderStatus()
                )
        );
    }

    private void publishDeliveryPartnerAssigned(Order order, DeliveryPartner deliveryPartner) {
        eventPublisher.publishEvent(
                new DeliveryPartnerAssignedEvent(
                        order.getId(),
                        deliveryPartner.getId()
                )
        );
    }

    private void validatePartnerServicesRestaurantCity(Order order, DeliveryPartner deliveryPartner) {

        City restaurantCity = order.getRestaurant().getCityEntity();
        City partnerCity = deliveryPartner.getCity();

        if (restaurantCity == null) {
            throw new IllegalArgumentException("Restaurant city is not configured");
        }

        if (partnerCity == null) {
            throw new IllegalArgumentException("Delivery partner city is not configured");
        }

        if (!restaurantCity.getId().equals(partnerCity.getId())) {
            throw new IllegalArgumentException(
                    "Delivery partner city does not match restaurant city. Restaurant city: "
                            + restaurantCity.getName()
                            + ", partner city: "
                            + partnerCity.getName()
            );
        }
    }

}
