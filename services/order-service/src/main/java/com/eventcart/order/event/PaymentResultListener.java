package com.eventcart.order.event;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that applies payment outcomes to orders.
 */
@Component
public class PaymentResultListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final OrderService orderService;

    /**
     * Creates a payment result listener.
     *
     * @param orderService order business service
     */
    public PaymentResultListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Handles successful payment events.
     *
     * @param event payment-completed event consumed from Kafka
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.payment-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Consumed PaymentCompleted event eventId={} orderId={} paymentId={}",
                event.metadata().eventId(), event.orderId(), event.paymentId());
        orderService.markPaymentCompleted(event);
    }

    /**
     * Handles failed payment events.
     *
     * @param event payment-failed event consumed from Kafka
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.payment-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Consumed PaymentFailed event eventId={} orderId={} paymentId={} reason={}",
                event.metadata().eventId(), event.orderId(), event.paymentId(), event.reason());
        orderService.markPaymentFailed(event);
    }
}
