package com.eventcart.inventory.event;

import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that reacts to order-created events.
 */
@Component
public class OrderCreatedListener {
    private final InventoryService inventoryService;

    /**
     * Creates an order-created listener.
     *
     * @param inventoryService inventory business service
     */
    public OrderCreatedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Handles an order-created event by attempting to reserve stock.
     *
     * @param event order-created event consumed from Kafka
     */
    @KafkaListener(topics = "${eventcart.kafka.topics.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(OrderCreatedEvent event) {
        inventoryService.reserveInventory(event);
    }
}
