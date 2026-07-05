package com.sumit.fooddelivery.dto.response;

import com.sumit.fooddelivery.enums.NotificationRecipientType;
import com.sumit.fooddelivery.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private Long orderId;

    private NotificationRecipientType recipientType;

    private Long recipientId;

    private String recipientName;

    private String message;

    private NotificationStatus status;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;
}
