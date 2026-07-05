package com.sumit.fooddelivery.listener;

import com.sumit.fooddelivery.entity.DeliveryPartner;
import com.sumit.fooddelivery.entity.Notification;
import com.sumit.fooddelivery.entity.Order;
import com.sumit.fooddelivery.enums.NotificationRecipientType;
import com.sumit.fooddelivery.enums.NotificationStatus;
import com.sumit.fooddelivery.event.DeliveryPartnerAssignedEvent;
import com.sumit.fooddelivery.event.OrderStatusChangedEvent;
import com.sumit.fooddelivery.repository.NotificationRepository;
import com.sumit.fooddelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {

        Order order = orderRepository.findById(event.orderId()).orElse(null);

        if (order == null) {
            return;
        }

        String statusText = event.oldStatus() == null
                ? "created with status " + event.newStatus()
                : "moved from " + event.oldStatus() + " to " + event.newStatus();

        String message = "Order #" + order.getId() + " has " + statusText + ".";

        List<Notification> notifications = new ArrayList<>();

        notifications.add(buildNotification(
                order,
                NotificationRecipientType.CUSTOMER,
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                "Your " + message
        ));

        notifications.add(buildNotification(
                order,
                NotificationRecipientType.RESTAURANT,
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                message
        ));

        if (order.getDeliveryPartner() != null) {
            notifications.add(buildNotification(
                    order,
                    NotificationRecipientType.DELIVERY_PARTNER,
                    order.getDeliveryPartner().getId(),
                    order.getDeliveryPartner().getName(),
                    message
            ));
        }

        notificationRepository.saveAll(notifications);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryPartnerAssigned(DeliveryPartnerAssignedEvent event) {

        Order order = orderRepository.findById(event.orderId()).orElse(null);

        if (order == null || order.getDeliveryPartner() == null) {
            return;
        }

        DeliveryPartner partner = order.getDeliveryPartner();

        String customerMessage = "Delivery partner " + partner.getName()
                + " has been assigned to your order #" + order.getId() + ".";

        String restaurantMessage = "Delivery partner " + partner.getName()
                + " has been assigned to order #" + order.getId() + ".";

        String partnerMessage = "You have been assigned order #" + order.getId()
                + " from " + order.getRestaurant().getName() + ".";

        List<Notification> notifications = new ArrayList<>();

        notifications.add(buildNotification(
                order,
                NotificationRecipientType.CUSTOMER,
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                customerMessage
        ));

        notifications.add(buildNotification(
                order,
                NotificationRecipientType.RESTAURANT,
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                restaurantMessage
        ));

        notifications.add(buildNotification(
                order,
                NotificationRecipientType.DELIVERY_PARTNER,
                partner.getId(),
                partner.getName(),
                partnerMessage
        ));

        notificationRepository.saveAll(notifications);
    }

    private Notification buildNotification(
            Order order,
            NotificationRecipientType recipientType,
            Long recipientId,
            String recipientName,
            String message
    ) {
        Notification notification = new Notification();
        notification.setOrder(order);
        notification.setRecipientType(recipientType);
        notification.setRecipientId(recipientId);
        notification.setRecipientName(recipientName);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.UNREAD);

        return notification;
    }
}
