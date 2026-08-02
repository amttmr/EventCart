package com.eventcart.inventory.event;

import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that compensates reserved stock after payment failure.
 */
@Component
public class PaymentFailedListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentFailedListener.class);

    private final InventoryService inventoryService;

    /**
     * Creates a payment-failed listener.
     *
     * @param inventoryService inventory business service
     */
    public PaymentFailedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Handles payment-failed events by releasing any stock reserved for the order.
     *
     * @param event payment-failed event consumed from Kafka
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.payment-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handle(PaymentFailedEvent event) {
        log.info("Consumed PaymentFailed event orderId={} paymentId={} eventId={} reason={}",
                event.orderId(), event.paymentId(), event.metadata().eventId(), event.reason());
        inventoryService.releaseReservationAfterPaymentFailure(event);
    }
}
