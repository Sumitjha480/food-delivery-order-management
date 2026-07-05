package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private String customerName;

    private String restaurantName;

    private String deliveryPartnerName;

    private BigDecimal totalAmount;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private List<OrderItemResponse> items;
}