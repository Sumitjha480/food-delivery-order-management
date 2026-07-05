package com.sumit.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    private Long menuItemId;

    private String menuItemName;

    private Integer quantity;

    private BigDecimal price;
}