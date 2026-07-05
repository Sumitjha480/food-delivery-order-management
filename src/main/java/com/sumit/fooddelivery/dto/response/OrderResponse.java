package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentMethod;
import com.sumit.fooddelivery.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private PaymentMethod paymentMethod;

    private String paymentReference;

    private String paymentFailureReason;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime preparingAt;

    private LocalDateTime outForDeliveryAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    private List<OrderItemResponse> items;
}
