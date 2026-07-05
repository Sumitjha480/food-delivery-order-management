package com.sumit.fooddelivery.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Restaurant extends BaseEntity {

    private String name;

    private String city;

    private String address;

    private boolean active;

}