package com.eventcart.order.event;

import com.eventcart.common.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes order domain events to Kafka.
 */
@Component
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderCreatedTopic;

    /**
     * Creates an order event publisher.
     *
     * @param kafkaTemplate Kafka template for order-created events
     * @param orderCreatedTopic configured order-created topic name
     */
    public OrderEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${eventcart.kafka.topics.order-created}") String orderCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    /**
     * Publishes an order-created event.
     *
     * @param event event payload to publish
     */
    public CompletableFuture<SendResult<String, Object>> publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreated event orderId={} eventId={} topic={}",
                event.orderId(), event.metadata().eventId(), orderCreatedTopic);
        var future = kafkaTemplate.send(orderCreatedTopic, event.orderId(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderCreated event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), orderCreatedTopic, ex);
            } else {
                log.debug("Published OrderCreated event orderId={} eventId={} topic={}",
                        event.orderId(), event.metadata().eventId(), orderCreatedTopic);
            }
        });
        return future;
    }
}
