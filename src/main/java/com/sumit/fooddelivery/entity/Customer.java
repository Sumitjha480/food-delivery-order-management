package com.sumit.fooddelivery.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Customer extends BaseEntity {

    private String name;

    private String email;

    private String phone;

}
