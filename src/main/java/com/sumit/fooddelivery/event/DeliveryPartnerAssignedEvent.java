package com.sumit.fooddelivery.event;

public record DeliveryPartnerAssignedEvent(
        Long orderId,
        Long deliveryPartnerId
) {
}
