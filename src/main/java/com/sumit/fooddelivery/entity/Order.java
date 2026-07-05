package com.sumit.fooddelivery.entity;

import com.sumit.fooddelivery.enums.OrderStatus;
import com.sumit.fooddelivery.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(optional = false)
    private Customer customer;

    @ManyToOne(optional = false)
    private Restaurant restaurant;

    @ManyToOne
    private DeliveryPartner deliveryPartner;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
}