package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryResponse {

    private Long id;

    private Long orderId;

    private OrderStatus oldStatus;

    private OrderStatus newStatus;

    private String changedByUsername;

    private String note;

    private LocalDateTime changedAt;
}
