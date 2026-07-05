package com.sumit.fooddelivery.entity;

import com.sumit.fooddelivery.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}