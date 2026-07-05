package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Order;
import com.sumit.fooddelivery.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select o
            from Order o
            join o.customer c
            join o.restaurant r
            left join o.deliveryPartner dp
            left join r.owner owner
            where (:status is null or o.orderStatus = :status)
              and (:restaurantId is null or r.id = :restaurantId)
              and (:customerId is null or c.id = :customerId)
              and (:deliveryPartnerId is null or dp.id = :deliveryPartnerId)
              and (:restaurantOwnerUsername is null or owner.username = :restaurantOwnerUsername)
            order by o.id desc
            """)
    List<Order> searchOrders(
            @Param("status") OrderStatus status,
            @Param("restaurantId") Long restaurantId,
            @Param("customerId") Long customerId,
            @Param("deliveryPartnerId") Long deliveryPartnerId,
            @Param("restaurantOwnerUsername") String restaurantOwnerUsername
    );
}
