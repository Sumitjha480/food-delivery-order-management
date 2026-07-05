package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_Username(String username);

    boolean existsByUser_Id(Long userId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);
}
