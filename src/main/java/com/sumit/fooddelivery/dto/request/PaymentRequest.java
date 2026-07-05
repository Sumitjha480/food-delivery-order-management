package com.sumit.fooddelivery.dto.request;

import com.sumit.fooddelivery.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotBlank(message = "Payment token is required")
    @Size(max = 100, message = "Payment token cannot exceed 100 characters")
    private String token;
}
