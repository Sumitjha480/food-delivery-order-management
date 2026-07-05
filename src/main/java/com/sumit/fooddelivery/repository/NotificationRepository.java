package com.sumit.fooddelivery.repository;

import com.sumit.fooddelivery.entity.Notification;
import com.sumit.fooddelivery.enums.NotificationRecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByOrder_IdOrderBySentAtDesc(Long orderId);

    List<Notification> findByRecipientTypeAndRecipientIdOrderBySentAtDesc(
            NotificationRecipientType recipientType,
            Long recipientId
    );
}
