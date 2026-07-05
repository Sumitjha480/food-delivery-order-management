package com.sumit.fooddelivery.entity;

import com.sumit.fooddelivery.enums.RestaurantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "restaurants")
public class Restaurant extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status;

    @Column(nullable = false)
    private Integer estimatedDeliveryTime;

}