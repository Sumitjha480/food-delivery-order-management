package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    @EntityGraph(attributePaths = {"order", "customer", "restaurant"})
    List<Review> findAll();

    @Override
    @EntityGraph(attributePaths = {"order", "customer", "restaurant"})
    Optional<Review> findById(Long id);

    @EntityGraph(attributePaths = {"order", "customer", "restaurant"})
    Optional<Review> findByOrder_Id(Long orderId);

    boolean existsByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"order", "customer", "restaurant"})
    List<Review> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId);

    @EntityGraph(attributePaths = {"order", "customer", "restaurant"})
    List<Review> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
}
