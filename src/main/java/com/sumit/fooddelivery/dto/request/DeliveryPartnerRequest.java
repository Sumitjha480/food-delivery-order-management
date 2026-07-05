package com.sumit.fooddelivery.dto.request;

import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryPartnerRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Delivery partner name is required")
    private String name;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @NotNull(message = "City id is required")
    private Long cityId;

    private DeliveryPartnerStatus status;
}
