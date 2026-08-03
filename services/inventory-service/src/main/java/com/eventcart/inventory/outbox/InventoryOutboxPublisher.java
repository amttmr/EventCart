package com.eventcart.inventory.outbox;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.inventory.event.InventoryEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled publisher that drains inventory outbox events to Kafka.
 */
@Component
public class InventoryOutboxPublisher {
    private static final int MAX_ATTEMPTS = 10;
    private static final Logger log = LoggerFactory.getLogger(InventoryOutboxPublisher.class);

    private final InventoryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final InventoryEventPublisher eventPublisher;

    /**
     * Creates an inventory outbox publisher.
     *
     * @param outboxRepository repository for pending events
     * @param objectMapper JSON mapper for event payloads
     * @param eventPublisher Kafka publisher for inventory events
     */
    public InventoryOutboxPublisher(
            InventoryOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            InventoryEventPublisher eventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes pending inventory outbox events at a fixed interval.
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
    private void publishOne(InventoryOutboxEventDocument outbox) {
        try {
            publishPayload(outbox);
            outbox.setStatus(OutboxEventStatus.PUBLISHED);
            outbox.setPublishedAt(Instant.now());
            outbox.setLastError(null);
            outboxRepository.save(outbox);
            log.info("Inventory outbox event published outboxId={} aggregateId={} topic={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getTopic());
        } catch (Exception ex) {
            outbox.setPublishAttempts(outbox.getPublishAttempts() + 1);
            outbox.setLastError(ex.getMessage());
            if (outbox.getPublishAttempts() >= MAX_ATTEMPTS) {
                outbox.setStatus(OutboxEventStatus.FAILED);
            }
            outboxRepository.save(outbox);
            log.warn("Inventory outbox event publish failed outboxId={} aggregateId={} attempts={} status={}",
                    outbox.getId(), outbox.getAggregateId(), outbox.getPublishAttempts(), outbox.getStatus(), ex);
        }
    }

    /**
     * Deserializes and publishes one stored inventory event.
     *
     * @param outbox pending outbox event
     * @throws Exception when deserialization or Kafka publication fails
     */
    private void publishPayload(InventoryOutboxEventDocument outbox) throws Exception {
        if (InventoryReservedEvent.EVENT_TYPE.equals(outbox.getEventType())) {
            InventoryReservedEvent event = objectMapper.readValue(outbox.getPayloadJson(), InventoryReservedEvent.class);
            eventPublisher.publishInventoryReserved(event).join();
            return;
        }
        if (InventoryReservationFailedEvent.EVENT_TYPE.equals(outbox.getEventType())) {
            InventoryReservationFailedEvent event =
                    objectMapper.readValue(outbox.getPayloadJson(), InventoryReservationFailedEvent.class);
            eventPublisher.publishInventoryFailed(event).join();
            return;
        }
        throw new IllegalArgumentException("Unsupported inventory outbox event type: " + outbox.getEventType());
    }
}
