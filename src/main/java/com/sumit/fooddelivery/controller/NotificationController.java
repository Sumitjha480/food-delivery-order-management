package com.sumit.fooddelivery.controller;

import com.sumit.fooddelivery.dto.response.NotificationResponse;
import com.sumit.fooddelivery.entity.Notification;
import com.sumit.fooddelivery.enums.NotificationRecipientType;
import com.sumit.fooddelivery.enums.NotificationStatus;
import com.sumit.fooddelivery.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "sentAt"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/order/{orderId}")
    public List<NotificationResponse> getByOrder(@PathVariable Long orderId) {
        return notificationRepository.findByOrder_IdOrderBySentAtDesc(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/recipient")
    public List<NotificationResponse> getByRecipient(
            @RequestParam NotificationRecipientType recipientType,
            @RequestParam Long recipientId
    ) {
        return notificationRepository
                .findByRecipientTypeAndRecipientIdOrderBySentAtDesc(recipientType, recipientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        return mapToResponse(notificationRepository.save(notification));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .orderId(notification.getOrder().getId())
                .recipientType(notification.getRecipientType())
                .recipientId(notification.getRecipientId())
                .recipientName(notification.getRecipientName())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
