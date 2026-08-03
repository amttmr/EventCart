package com.eventcart.payment.outbox;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.payment.event.PaymentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled publisher that drains payment outbox events to Kafka.
 */
@Component
public class PaymentOutboxPublisher {
    private static final int MAX_ATTEMPTS = 10;
    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxPublisher.class);

    private final PaymentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Creates a payment outbox publisher.
     *
     * @param outboxRepository repository for pending events
     * @param objectMapper JSON mapper for event payloads
     * @param eventPublisher Kafka publisher for payment events
     */
    public PaymentOutboxPublisher(
            PaymentOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            PaymentEventPublisher eventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes pending payment outbox events at a fixed interval.
     */
    @Scheduled(
            fixedDelayString = "${eventcart.outbox.poll-delay:5s}",
            initialDelayString = "${eventcart.outbox.initial-delay:0s}"
    )
    public void publishPendingEvents() {
        outboxRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING)
                .forEach(this::publishOne);
    }

    /**
     * Publishes one outbox event and updates its status.
     *
     * @param outbox pending outbox event
     */
    private void publishOne(PaymentOutboxEventDocument outbox) {
        try {
            publishPayload(outbox);
            outbox.setStatus(OutboxEventStatus.PUBLISHED);
            outbox.setPublishedAt(Instant.now());
            outbox.setLastError(null);
            outboxRepository.save(outbox);
            log.info("Payment outbox event published outboxId={} aggregateId={} topic={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getTopic());
        } catch (Exception ex) {
            outbox.setPublishAttempts(outbox.getPublishAttempts() + 1);
            outbox.setLastError(ex.getMessage());
            if (outbox.getPublishAttempts() >= MAX_ATTEMPTS) {
                outbox.setStatus(OutboxEventStatus.FAILED);
            }
            outboxRepository.save(outbox);
            log.warn("Payment outbox event publish failed outboxId={} aggregateId={} attempts={} status={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getPublishAttempts(), outbox.getStatus(), ex);
        }
    }

    /**
     * Deserializes and publishes one stored payment event.
     *
     * @param outbox pending outbox event
     * @throws Exception when deserialization or Kafka publication fails
     */
    private void publishPayload(PaymentOutboxEventDocument outbox) throws Exception {
        if (PaymentCompletedEvent.EVENT_TYPE.equals(outbox.getEventType())) {
            PaymentCompletedEvent event = objectMapper.readValue(outbox.getPayloadJson(), PaymentCompletedEvent.class);
            eventPublisher.publishPaymentCompleted(event).join();
            return;
        }
        if (PaymentFailedEvent.EVENT_TYPE.equals(outbox.getEventType())) {
            PaymentFailedEvent event = objectMapper.readValue(outbox.getPayloadJson(), PaymentFailedEvent.class);
            eventPublisher.publishPaymentFailed(event).join();
            return;
        }
        throw new IllegalArgumentException("Unsupported payment outbox event type: " + outbox.getEventType());
    }
}
