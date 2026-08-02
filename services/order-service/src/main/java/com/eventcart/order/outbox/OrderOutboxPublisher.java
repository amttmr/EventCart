package com.eventcart.order.outbox;

import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.order.event.OrderEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled publisher that drains pending order outbox events to Kafka.
 */
@Component
public class OrderOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Creates an order outbox publisher.
     *
     * @param outboxRepository repository that stores pending events
     * @param objectMapper JSON mapper for event payloads
     * @param orderEventPublisher Kafka publisher for order events
     */
    public OrderOutboxPublisher(
            OrderOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            OrderEventPublisher orderEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Publishes pending outbox events at a fixed interval.
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
    private void publishOne(OrderOutboxEventDocument outbox) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(outbox.getPayloadJson(), OrderCreatedEvent.class);
            orderEventPublisher.publishOrderCreated(event).join();
            outbox.setStatus(OutboxEventStatus.PUBLISHED);
            outbox.setPublishedAt(Instant.now());
            outbox.setLastError(null);
            outboxRepository.save(outbox);
            log.info("Outbox event published outboxId={} orderId={} topic={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getTopic());
        } catch (Exception ex) {
            outbox.setPublishAttempts(outbox.getPublishAttempts() + 1);
            outbox.setLastError(ex.getMessage());
            if (outbox.getPublishAttempts() >= 10) {
                outbox.setStatus(OutboxEventStatus.FAILED);
            }
            outboxRepository.save(outbox);
            log.warn("Outbox event publish failed outboxId={} orderId={} attempts={} status={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getPublishAttempts(), outbox.getStatus(), ex);
        }
    }
}
