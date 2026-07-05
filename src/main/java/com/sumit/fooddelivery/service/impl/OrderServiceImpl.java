package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.OrderItemRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.response.OrderItemResponse;
import com.sumit.fooddelivery.dto.response.OrderResponse;
import com.sumit.fooddelivery.entity.*;
import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentStatus;
import com.sumit.fooddelivery.repository.*;
import com.sumit.fooddelivery.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public OrderResponse create(OrderRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        DeliveryPartner deliveryPartner = null;

        if (request.getDeliveryPartnerId() != null) {
            deliveryPartner = deliveryPartnerRepository.findById(request.getDeliveryPartnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Delivery partner not found"));
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryPartner(deliveryPartner);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Menu item not found"));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException(
                        "Menu item " + menuItem.getId() + " does not belong to restaurant " + restaurant.getId()
                );
            }

            if (menuItem.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item: " + menuItem.getName());
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

        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

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

        validateCurrentStatus(order, OrderStatus.PLACED, "Only PLACED orders can be accepted");

        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse reject(Long id, String reason) {

        Order order = getOrderOrThrow(id);

        validateCurrentStatus(order, OrderStatus.PLACED, "Only PLACED orders can be rejected");

        restoreStock(order);

        order.setOrderStatus(OrderStatus.REJECTED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setRejectedAt(LocalDateTime.now());
        order.setRejectionReason(reason != null && !reason.isBlank()
                ? reason
                : "Rejected by restaurant");

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markPreparing(Long id) {

        Order order = getOrderOrThrow(id);

        validateCurrentStatus(order, OrderStatus.ACCEPTED, "Only ACCEPTED orders can move to PREPARING");

        order.setOrderStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markOutForDelivery(Long id) {

        Order order = getOrderOrThrow(id);

        validateCurrentStatus(order, OrderStatus.PREPARING, "Only PREPARING orders can move to OUT_FOR_DELIVERY");

        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setOutForDeliveryAt(LocalDateTime.now());

        if (order.getDeliveryPartner() != null) {
            order.getDeliveryPartner().setStatus(DeliveryPartnerStatus.BUSY);
            deliveryPartnerRepository.save(order.getDeliveryPartner());
        }

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder, savedOrder.getOrderItems());
    }

    @Override
    public OrderResponse markDelivered(Long id) {

        Order order = getOrderOrThrow(id);

        validateCurrentStatus(order, OrderStatus.OUT_FOR_DELIVERY, "Only OUT_FOR_DELIVERY orders can be delivered");

        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setDeliveredAt(LocalDateTime.now());

        if (order.getDeliveryPartner() != null) {
            order.getDeliveryPartner().setStatus(DeliveryPartnerStatus.AVAILABLE);
            deliveryPartnerRepository.save(order.getDeliveryPartner());
        }

        Order savedOrder = orderRepository.save(order);

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

    private void validateCurrentStatus(Order order, OrderStatus expectedStatus, String message) {

        if (order.getOrderStatus() != expectedStatus) {
            throw new IllegalArgumentException(
                    message + ". Current status is " + order.getOrderStatus()
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
                .acceptedAt(order.getAcceptedAt())
                .preparingAt(order.getPreparingAt())
                .outForDeliveryAt(order.getOutForDeliveryAt())
                .deliveredAt(order.getDeliveredAt())
                .rejectedAt(order.getRejectedAt())
                .rejectionReason(order.getRejectionReason())
                .items(itemResponses)
                .build();
    }
}
