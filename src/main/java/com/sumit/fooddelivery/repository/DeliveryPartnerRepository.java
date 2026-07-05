package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.DeliveryPartner;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DeliveryPartner d where d.id = :id")
    Optional<DeliveryPartner> findByIdForUpdate(@Param("id") Long id);

    List<DeliveryPartner> findByCity_Id(Long cityId);

    Optional<DeliveryPartner> findByUser_Username(String username);

    boolean existsByUser_Id(Long userId);
}
