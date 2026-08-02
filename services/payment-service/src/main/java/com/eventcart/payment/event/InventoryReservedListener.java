package com.eventcart.payment.event;

import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that starts payment simulation after inventory is reserved.
 */
@Component
public class InventoryReservedListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservedListener.class);

    private final PaymentService paymentService;

    /**
     * Creates an inventory-reserved listener.
     *
     * @param paymentService payment business service
     */
    public InventoryReservedListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Handles an inventory-reserved event by simulating payment.
     *
     * @param event inventory-reserved event consumed from Kafka
     */
    @KafkaListener(topics = "${eventcart.kafka.topics.inventory-reserved}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(InventoryReservedEvent event) {
        log.info("Consumed InventoryReserved event orderId={} eventId={} amount={} currency={}",
                event.orderId(), event.metadata().eventId(), event.totalAmount(), event.currency());
        paymentService.processInventoryReserved(event);
    }
}
