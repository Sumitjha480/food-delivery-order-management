package com.sumit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Restaurant address is required")
    private String address;

    @NotNull(message = "City id is required")
    private Long cityId;

    @NotNull(message = "Estimated delivery time is required")
    @Positive(message = "Estimated delivery time must be positive")
    private Integer estimatedDeliveryTime;
}
