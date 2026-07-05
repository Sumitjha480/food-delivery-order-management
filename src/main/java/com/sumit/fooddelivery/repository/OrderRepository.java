package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}