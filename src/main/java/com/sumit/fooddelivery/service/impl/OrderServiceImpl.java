package com.sumit.fooddelivery.service.impl;

import com.sumit.fooddelivery.dto.request.OrderItemRequest;
import com.sumit.fooddelivery.dto.request.OrderRequest;
import com.sumit.fooddelivery.dto.response.OrderItemResponse;
import com.sumit.fooddelivery.dto.response.OrderResponse;
import com.sumit.fooddelivery.entity.*;
import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentStatus;
import com.sumit.fooddelivery.repository.*;
import com.sumit.fooddelivery.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

            if (menuItem.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item: " + menuItem.getName());
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
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(order -> mapToResponse(order, order.getOrderItems()))
                .toList();
    }

    @Override
    public OrderResponse getById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        return mapToResponse(order, order.getOrderItems());
    }

    @Override
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    // ---------------- mapping ----------------

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
                .items(itemResponses)
                .build();
    }
}