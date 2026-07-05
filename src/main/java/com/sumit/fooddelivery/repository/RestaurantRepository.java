package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}