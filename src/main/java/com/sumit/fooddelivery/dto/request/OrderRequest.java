package com.sumit.fooddelivery.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long restaurantId;

    private Long deliveryPartnerId;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;
}