package com.sumit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class OrderItem extends BaseEntity {

    @ManyToOne(optional = false)
    private Order order;

    @ManyToOne(optional = false)
    private MenuItem menuItem;

    private Integer quantity;

    private BigDecimal price;
}