package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

}
