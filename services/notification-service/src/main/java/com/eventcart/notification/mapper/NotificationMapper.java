package com.eventcart.notification.mapper;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.notification.domain.NotificationChannel;
import com.eventcart.notification.domain.NotificationDocument;
import com.eventcart.notification.domain.NotificationStatus;
import com.eventcart.notification.domain.NotificationType;
import com.eventcart.notification.dto.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps event payloads, notification documents, and API responses.
 */
@Component
public class NotificationMapper {
    /**
     * Creates a notification from an order-created event.
     *
     * @param event order-created event
     * @return unsaved notification document
     */
    public NotificationDocument fromOrderCreated(OrderCreatedEvent event) {
        return document(
                event.customerId(),
                event.orderId(),
                event.metadata().eventId(),
                event.metadata().correlationId(),
                NotificationType.ORDER_CREATED,
                "Order placed",
                "Your order " + event.orderId() + " has been placed and inventory reservation has started."
        );
    }

    /**
     * Creates a notification from an inventory-failed event.
     *
     * @param event inventory-failed event
     * @return unsaved notification document
     */
    public NotificationDocument fromInventoryFailed(InventoryReservationFailedEvent event) {
        return document(
                event.customerId(),
                event.orderId(),
                event.metadata().eventId(),
                event.metadata().correlationId(),
                NotificationType.INVENTORY_FAILED,
                "Inventory unavailable",
                "We could not reserve inventory for order " + event.orderId() + ": " + event.reason()
        );
    }

    /**
     * Creates a notification from a payment-completed event.
     *
     * @param event payment-completed event
     * @return unsaved notification document
     */
    public NotificationDocument fromPaymentCompleted(PaymentCompletedEvent event) {
        return document(
                event.customerId(),
                event.orderId(),
                event.metadata().eventId(),
                event.metadata().correlationId(),
                NotificationType.PAYMENT_COMPLETED,
                "Payment completed",
                "Payment was completed for order " + event.orderId() + "."
        );
    }

    /**
     * Creates a notification from a payment-failed event.
     *
     * @param event payment-failed event
     * @return unsaved notification document
     */
    public NotificationDocument fromPaymentFailed(PaymentFailedEvent event) {
        return document(
                event.customerId(),
                event.orderId(),
                event.metadata().eventId(),
                event.metadata().correlationId(),
                NotificationType.PAYMENT_FAILED,
                "Payment failed",
                "Payment failed for order " + event.orderId() + ": " + event.reason()
        );
    }

    /**
     * Converts a notification document into a public API response.
     *
     * @param notification persisted notification
     * @return API response
     */
    public NotificationResponse toResponse(NotificationDocument notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCustomerId(),
                notification.getOrderId(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCorrelationId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    /**
     * Converts notification documents into API responses.
     *
     * @param notifications persisted notifications
     * @return API responses
     */
    public List<NotificationResponse> toResponses(List<NotificationDocument> notifications) {
        return notifications.stream().map(this::toResponse).toList();
    }

    /**
     * Creates a common notification document.
     *
     * @param customerId customer ID
     * @param orderId order ID
     * @param sourceEventId source Kafka event ID
     * @param correlationId correlation ID
     * @param type notification type
     * @param title title text
     * @param message body text
     * @return unsaved notification document
     */
    private NotificationDocument document(
            String customerId,
            String orderId,
            String sourceEventId,
            String correlationId,
            NotificationType type,
            String title,
            String message
    ) {
        NotificationDocument notification = new NotificationDocument();
        notification.setCustomerId(customerId);
        notification.setOrderId(orderId);
        notification.setSourceEventId(sourceEventId);
        notification.setCorrelationId(correlationId);
        notification.setType(type);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setTitle(title);
        notification.setMessage(message);
        return notification;
    }
}
