package com.eventcart.inventory.event;

import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.web.observability.CorrelationIdContext;
import com.eventcart.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that reacts to order-created events.
 */
@Component
public class OrderCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

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
        MDC.put(CorrelationIdContext.MDC_KEY, event.metadata().correlationId());
        try {
            log.info("Consumed OrderCreated event orderId={} eventId={} itemCount={}",
                    event.orderId(), event.metadata().eventId(), event.items().size());
            inventoryService.reserveInventory(event);
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }
}
