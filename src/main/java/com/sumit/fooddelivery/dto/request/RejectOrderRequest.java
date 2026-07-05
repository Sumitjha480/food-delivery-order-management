package com.sumit.fooddelivery.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectOrderRequest {

    @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
    private String reason;
}
