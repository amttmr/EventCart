package com.eventcart.inventory.event;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes inventory reservation result events to Kafka.
 */
@Component
public class InventoryEventPublisher {
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
    public void publishInventoryReserved(InventoryReservedEvent event) {
        kafkaTemplate.send(inventoryReservedTopic, event.orderId(), event);
    }

    /**
     * Publishes a failed reservation event.
     *
     * @param event event payload to publish
     */
    public void publishInventoryFailed(InventoryReservationFailedEvent event) {
        kafkaTemplate.send(inventoryFailedTopic, event.orderId(), event);
    }
}
