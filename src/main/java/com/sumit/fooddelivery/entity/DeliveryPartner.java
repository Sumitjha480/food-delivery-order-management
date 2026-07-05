package com.sumit.fooddelivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private boolean available;

}