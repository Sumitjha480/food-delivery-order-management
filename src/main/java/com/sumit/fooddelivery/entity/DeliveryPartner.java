package com.sumit.fooddelivery.entity;

import com.sumit.fooddelivery.enums.DeliveryPartnerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeliveryPartnerStatus status;
}
