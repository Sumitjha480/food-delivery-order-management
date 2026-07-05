package com.sumit.fooddelivery.entity;

import com.sumit.fooddelivery.enums.NotificationRecipientType;
import com.sumit.fooddelivery.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationRecipientType recipientType;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, length = 150)
    private String recipientName;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = NotificationStatus.UNREAD;
        }

        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
