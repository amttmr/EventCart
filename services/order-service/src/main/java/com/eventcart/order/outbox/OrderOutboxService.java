package com.eventcart.order.outbox;

import com.eventcart.common.events.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that stores order events in the outbox before Kafka publication.
 */
@Service
public class OrderOutboxService {
    private static final Logger log = LoggerFactory.getLogger(OrderOutboxService.class);

    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String orderCreatedTopic;

    /**
     * Creates an order outbox service.
     *
     * @param outboxRepository repository for outbox persistence
     * @param objectMapper JSON mapper for event payloads
     * @param orderCreatedTopic configured order-created Kafka topic
     */
    public OrderOutboxService(
            OrderOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${eventcart.kafka.topics.order-created}") String orderCreatedTopic
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    /**
     * Stores an order-created event in the outbox.
     *
     * @param event event to publish later
     * @return saved outbox event
     */
    public OrderOutboxEventDocument enqueueOrderCreated(OrderCreatedEvent event) {
        OrderOutboxEventDocument outbox = new OrderOutboxEventDocument();
        outbox.setAggregateType("ORDER");
        outbox.setAggregateId(event.orderId());
        outbox.setEventType(event.metadata().eventType());
        outbox.setTopic(orderCreatedTopic);
        outbox.setEventKey(event.orderId());
        outbox.setPayloadJson(toJson(event));
        outbox.setStatus(OutboxEventStatus.PENDING);
        OrderOutboxEventDocument saved = outboxRepository.save(outbox);
        log.info("OrderCreated event stored in outbox orderId={} outboxId={} eventId={}",
                event.orderId(), saved.getId(), event.metadata().eventId());
        return saved;
    }

    /**
     * Serializes an event for outbox storage.
     *
     * @param event event payload
     * @return JSON payload
     */
    private String toJson(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize OrderCreatedEvent for outbox", ex);
        }
    }
}
