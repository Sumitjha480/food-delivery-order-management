package com.sumit.fooddelivery.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Long restaurantId;
}