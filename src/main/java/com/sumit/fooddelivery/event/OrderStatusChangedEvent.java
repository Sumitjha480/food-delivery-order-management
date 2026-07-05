package com.sumit.fooddelivery.event;

import com.sumit.fooddelivery.enums.OrderStatus;

public record OrderStatusChangedEvent(
        Long orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus
) {
}
