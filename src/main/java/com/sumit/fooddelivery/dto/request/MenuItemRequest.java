package com.sumit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MenuItemRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer stock;
}
