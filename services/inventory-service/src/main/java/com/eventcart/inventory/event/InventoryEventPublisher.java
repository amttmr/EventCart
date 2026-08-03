package com.eventcart.inventory.event;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes inventory reservation result events to Kafka.
 */
@Component
public class InventoryEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryReservedTopic;
    private final String inventoryFailedTopic;

    /**
     * Creates an inventory event publisher.
     *
     * @param kafkaTemplate Kafka template for inventory result events
     * @param inventoryReservedTopic configured inventory-reserved topic name
     * @param inventoryFailedTopic configured inventory-failed topic name
     */
    public InventoryEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${eventcart.kafka.topics.inventory-reserved}") String inventoryReservedTopic,
            @Value("${eventcart.kafka.topics.inventory-failed}") String inventoryFailedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryReservedTopic = inventoryReservedTopic;
        this.inventoryFailedTopic = inventoryFailedTopic;
    }

    /**
     * Publishes a successful reservation event.
     *
     * @param event event payload to publish
     */
    public CompletableFuture<?> publishInventoryReserved(InventoryReservedEvent event) {
        log.info("Publishing InventoryReserved event orderId={} eventId={} topic={}",
                event.orderId(), event.metadata().eventId(), inventoryReservedTopic);
        var future = kafkaTemplate.send(inventoryReservedTopic, event.orderId(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish InventoryReserved event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), inventoryReservedTopic, ex);
            } else {
                log.debug("Published InventoryReserved event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), inventoryReservedTopic);
            }
        });
        return future;
    }

    /**
     * Publishes a failed reservation event.
     *
     * @param event event payload to publish
     */
    public CompletableFuture<?> publishInventoryFailed(InventoryReservationFailedEvent event) {
        log.info("Publishing InventoryReservationFailed event orderId={} eventId={} topic={}",
                event.orderId(), event.metadata().eventId(), inventoryFailedTopic);
        var future = kafkaTemplate.send(inventoryFailedTopic, event.orderId(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish InventoryReservationFailed event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), inventoryFailedTopic, ex);
            } else {
                log.debug("Published InventoryReservationFailed event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), inventoryFailedTopic);
            }
        });
        return future;
    }
}
