package com.eventcart.payment.outbox;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that stores payment result events before Kafka publication.
 */
@Service
public class PaymentOutboxService {
    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxService.class);

    private final PaymentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String paymentCompletedTopic;
    private final String paymentFailedTopic;

    /**
     * Creates a payment outbox service.
     *
     * @param outboxRepository repository for payment outbox events
     * @param objectMapper JSON mapper for event payloads
     * @param paymentCompletedTopic configured payment-completed topic
     * @param paymentFailedTopic configured payment-failed topic
     */
    public PaymentOutboxService(
            PaymentOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${eventcart.kafka.topics.payment-completed}") String paymentCompletedTopic,
            @Value("${eventcart.kafka.topics.payment-failed}") String paymentFailedTopic
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.paymentCompletedTopic = paymentCompletedTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    /**
     * Stores a payment-completed event in the outbox.
     *
     * @param event payment-completed event
     * @return saved outbox event
     */
    public PaymentOutboxEventDocument enqueuePaymentCompleted(PaymentCompletedEvent event) {
        PaymentOutboxEventDocument saved = saveEvent(
                "PAYMENT",
                event.paymentId(),
                event.metadata().eventType(),
                paymentCompletedTopic,
                event.orderId(),
                event
        );
        log.info("PaymentCompleted event stored in outbox orderId={} paymentId={} outboxId={} eventId={}",
                event.orderId(), event.paymentId(), saved.getId(), event.metadata().eventId());
        return saved;
    }

    /**
     * Stores a payment-failed event in the outbox.
     *
     * @param event payment-failed event
     * @return saved outbox event
     */
    public PaymentOutboxEventDocument enqueuePaymentFailed(PaymentFailedEvent event) {
        PaymentOutboxEventDocument saved = saveEvent(
                "PAYMENT",
                event.paymentId(),
                event.metadata().eventType(),
                paymentFailedTopic,
                event.orderId(),
                event
        );
        log.info("PaymentFailed event stored in outbox orderId={} paymentId={} outboxId={} eventId={}",
                event.orderId(), event.paymentId(), saved.getId(), event.metadata().eventId());
        return saved;
    }

    /**
     * Creates and saves a generic payment outbox row.
     *
     * @param aggregateType aggregate type
     * @param aggregateId aggregate ID
     * @param eventType event type
     * @param topic Kafka topic
     * @param eventKey Kafka event key
     * @param payload event payload
     * @return saved outbox document
     */
    private PaymentOutboxEventDocument saveEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            Object payload
    ) {
        PaymentOutboxEventDocument outbox = new PaymentOutboxEventDocument();
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setTopic(topic);
        outbox.setEventKey(eventKey);
        outbox.setPayloadJson(toJson(payload));
        outbox.setStatus(OutboxEventStatus.PENDING);
        return outboxRepository.save(outbox);
    }

    /**
     * Serializes an event payload for durable outbox storage.
     *
     * @param payload event payload
     * @return JSON payload
     */
    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize payment event for outbox", ex);
        }
    }
}
