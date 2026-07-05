package com.sumit.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long id;

    private Long orderId;

    private Long customerId;

    private String customerName;

    private Long restaurantId;

    private String restaurantName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
