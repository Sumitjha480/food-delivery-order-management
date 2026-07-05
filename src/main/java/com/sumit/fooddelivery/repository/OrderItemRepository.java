package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
