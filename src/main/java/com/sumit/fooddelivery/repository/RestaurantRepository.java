package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCityEntity_Id(Long cityId);
}
