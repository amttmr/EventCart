package com.eventcart.notification.event;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.common.web.observability.CorrelationIdContext;
import com.eventcart.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that records payment-related customer notifications.
 */
@Component
public class PaymentNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationListener.class);

    private final NotificationService notificationService;

    /**
     * Creates a payment notification listener.
     *
     * @param notificationService notification business service
     */
    public PaymentNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Handles payment-completed events.
     *
     * @param event payment-completed event
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.payment-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentCompletedKafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        withCorrelation(event.metadata().correlationId(), () -> {
            log.info("Consumed PaymentCompleted event for notification orderId={} paymentId={} eventId={}",
                    event.orderId(), event.paymentId(), event.metadata().eventId());
            notificationService.recordPaymentCompleted(event);
        });
    }

    /**
     * Handles payment-failed events.
     *
     * @param event payment-failed event
     */
    @KafkaListener(
            topics = "${eventcart.kafka.topics.payment-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        withCorrelation(event.metadata().correlationId(), () -> {
            log.info("Consumed PaymentFailed event for notification orderId={} paymentId={} eventId={}",
                    event.orderId(), event.paymentId(), event.metadata().eventId());
            notificationService.recordPaymentFailed(event);
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
