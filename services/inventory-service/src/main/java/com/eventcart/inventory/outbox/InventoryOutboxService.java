package com.eventcart.inventory.outbox;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that stores inventory result events before Kafka publication.
 */
@Service
public class InventoryOutboxService {
    private static final Logger log = LoggerFactory.getLogger(InventoryOutboxService.class);

    private final InventoryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String inventoryReservedTopic;
    private final String inventoryFailedTopic;

    /**
     * Creates an inventory outbox service.
     *
     * @param outboxRepository repository for inventory outbox events
     * @param objectMapper JSON mapper for event payloads
     * @param inventoryReservedTopic configured inventory-reserved topic
     * @param inventoryFailedTopic configured inventory-failed topic
     */
    public InventoryOutboxService(
            InventoryOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${eventcart.kafka.topics.inventory-reserved}") String inventoryReservedTopic,
            @Value("${eventcart.kafka.topics.inventory-failed}") String inventoryFailedTopic
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.inventoryReservedTopic = inventoryReservedTopic;
        this.inventoryFailedTopic = inventoryFailedTopic;
    }

    /**
     * Stores an inventory-reserved event in the outbox.
     *
     * @param event inventory-reserved event
     * @return saved outbox event
     */
    public InventoryOutboxEventDocument enqueueInventoryReserved(InventoryReservedEvent event) {
        InventoryOutboxEventDocument saved = saveEvent(
                "INVENTORY_RESERVATION",
                event.orderId(),
                event.metadata().eventType(),
                inventoryReservedTopic,
                event.orderId(),
                event
        );
        log.info("InventoryReserved event stored in outbox orderId={} outboxId={} eventId={}",
                event.orderId(), saved.getId(), event.metadata().eventId());
        return saved;
    }

    /**
     * Stores an inventory-reservation-failed event in the outbox.
     *
     * @param event inventory-reservation-failed event
     * @return saved outbox event
     */
    public InventoryOutboxEventDocument enqueueInventoryFailed(InventoryReservationFailedEvent event) {
        InventoryOutboxEventDocument saved = saveEvent(
                "INVENTORY_RESERVATION",
                event.orderId(),
                event.metadata().eventType(),
                inventoryFailedTopic,
                event.orderId(),
                event
        );
        log.info("InventoryReservationFailed event stored in outbox orderId={} outboxId={} eventId={}",
                event.orderId(), saved.getId(), event.metadata().eventId());
        return saved;
    }

    /**
     * Creates and saves a generic inventory outbox row.
     *
     * @param aggregateType aggregate type
     * @param aggregateId aggregate ID
     * @param eventType event type
     * @param topic Kafka topic
     * @param eventKey Kafka event key
     * @param payload event payload
     * @return saved outbox document
     */
    private InventoryOutboxEventDocument saveEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            Object payload
    ) {
        InventoryOutboxEventDocument outbox = new InventoryOutboxEventDocument();
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
            throw new IllegalStateException("Unable to serialize inventory event for outbox", ex);
        }
    }
}
