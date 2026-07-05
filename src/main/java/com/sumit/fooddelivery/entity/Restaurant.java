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

    // Legacy/denormalized city name kept to avoid old DB column issues.
    @Column(nullable = false)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City cityEntity;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RestaurantStatus status;

    @Column(nullable = false)
    private Integer estimatedDeliveryTime;
}
