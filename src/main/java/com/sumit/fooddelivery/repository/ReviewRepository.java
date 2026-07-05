package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"customer", "restaurant"})
    List<Review> findAll();

    @EntityGraph(attributePaths = {"customer", "restaurant"})
    Optional<Review> findById(Long id);

}
