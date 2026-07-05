package com.sumit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class MenuItem extends BaseEntity {

    private String name;

    private BigDecimal price;

    private Integer stock;

    @Version
    private Long version;

    @ManyToOne
    private Restaurant restaurant;

}
