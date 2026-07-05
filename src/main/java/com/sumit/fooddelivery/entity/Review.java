package com.sumit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Review extends BaseEntity {

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Restaurant restaurant;

    private Integer rating;

    private String comment;
}
