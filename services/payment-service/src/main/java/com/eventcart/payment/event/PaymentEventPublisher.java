package com.eventcart.payment.event;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes payment result events to Kafka.
 */
@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String paymentCompletedTopic;
    private final String paymentFailedTopic;

    /**
     * Creates a payment event publisher.
     *
     * @param kafkaTemplate Kafka template for payment result events
     * @param paymentCompletedTopic configured payment-completed topic name
     * @param paymentFailedTopic configured payment-failed topic name
     */
    public PaymentEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${eventcart.kafka.topics.payment-completed}") String paymentCompletedTopic,
            @Value("${eventcart.kafka.topics.payment-failed}") String paymentFailedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentCompletedTopic = paymentCompletedTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    /**
     * Publishes a successful payment event.
     *
     * @param event event payload to publish
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompleted event orderId={} paymentId={} eventId={} topic={}",
                event.orderId(), event.paymentId(), event.metadata().eventId(), paymentCompletedTopic);
        var future = kafkaTemplate.send(paymentCompletedTopic, event.orderId(), event);
        if (future != null) {
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentCompleted event orderId={} paymentId={} topic={}",
                            event.orderId(), event.paymentId(), paymentCompletedTopic, ex);
                } else {
                    log.debug("Published PaymentCompleted event orderId={} paymentId={} topic={}",
                            event.orderId(), event.paymentId(), paymentCompletedTopic);
                }
            });
        }
    }

    /**
     * Publishes a failed payment event.
     *
     * @param event event payload to publish
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailed event orderId={} paymentId={} eventId={} topic={}",
                event.orderId(), event.paymentId(), event.metadata().eventId(), paymentFailedTopic);
        var future = kafkaTemplate.send(paymentFailedTopic, event.orderId(), event);
        if (future != null) {
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentFailed event orderId={} paymentId={} topic={}",
                            event.orderId(), event.paymentId(), paymentFailedTopic, ex);
                } else {
                    log.debug("Published PaymentFailed event orderId={} paymentId={} topic={}",
                            event.orderId(), event.paymentId(), paymentFailedTopic);
                }
            });
        }
    }
}
