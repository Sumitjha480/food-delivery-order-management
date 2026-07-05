package com.sumit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDeliveryPartnerRequest {

    @NotNull(message = "Delivery partner id is required")
    private Long deliveryPartnerId;
}
