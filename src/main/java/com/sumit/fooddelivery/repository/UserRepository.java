package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
