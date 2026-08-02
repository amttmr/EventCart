package com.eventcart.notification.event;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.web.observability.CorrelationIdContext;
import com.eventcart.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that records order-related customer notifications.
 */
@Component
public class OrderNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final NotificationService notificationService;

    /**
     * Creates an order notification listener.
     *
     * @param notificationService notification business service
     */
    public OrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Handles order-created events.
     *
     * @param event order-created event
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        withCorrelation(event.metadata().correlationId(), () -> {
            log.info("Consumed OrderCreated event for notification orderId={} eventId={}",
                    event.orderId(), event.metadata().eventId());
            notificationService.recordOrderCreated(event);
        });
    }

    /**
     * Handles inventory-failed events.
     *
     * @param event inventory-failed event
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.inventory-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryFailedKafkaListenerContainerFactory"
    )
    public void handleInventoryFailed(InventoryReservationFailedEvent event) {
        withCorrelation(event.metadata().correlationId(), () -> {
            log.info("Consumed InventoryReservationFailed event for notification orderId={} eventId={}",
                    event.orderId(), event.metadata().eventId());
            notificationService.recordInventoryFailed(event);
        });
    }

    /**
     * Runs listener work with the event correlation ID in MDC.
     *
     * @param correlationId correlation ID from event metadata
     * @param action listener action
     */
    private void withCorrelation(String correlationId, Runnable action) {
        MDC.put(CorrelationIdContext.MDC_KEY, correlationId);
        try {
            action.run();
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }
}
