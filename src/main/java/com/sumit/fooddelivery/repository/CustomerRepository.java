package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
