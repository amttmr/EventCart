package com.eventcart.order.event;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.web.observability.CorrelationIdContext;
import com.eventcart.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that applies inventory reservation outcomes to orders.
 */
@Component
public class InventoryResultListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryResultListener.class);

    private final OrderService orderService;

    /**
     * Creates an inventory result listener.
     *
     * @param orderService order business service
     */
    public InventoryResultListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Handles successful inventory reservation events.
     *
     * @param event inventory-reserved event consumed from Kafka
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.inventory-reserved}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservedKafkaListenerContainerFactory"
    )
    public void handleInventoryReserved(InventoryReservedEvent event) {
        MDC.put(CorrelationIdContext.MDC_KEY, event.metadata().correlationId());
        try {
            log.info("Consumed InventoryReserved event eventId={} orderId={} customerId={}",
                    event.metadata().eventId(), event.orderId(), event.customerId());
            orderService.markInventoryReserved(event);
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }

    /**
     * Handles failed inventory reservation events.
     *
     * @param event inventory-failed event consumed from Kafka
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.inventory-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryFailedKafkaListenerContainerFactory"
    )
    public void handleInventoryFailed(InventoryReservationFailedEvent event) {
        MDC.put(CorrelationIdContext.MDC_KEY, event.metadata().correlationId());
        try {
            log.info("Consumed InventoryReservationFailed event eventId={} orderId={} customerId={} reason={}",
                    event.metadata().eventId(), event.orderId(), event.customerId(), event.reason());
            orderService.markInventoryFailed(event);
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }
}
