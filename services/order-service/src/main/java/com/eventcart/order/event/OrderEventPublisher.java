package com.eventcart.order.event;

import com.eventcart.common.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order domain events to Kafka.
 */
@Component
public class OrderEventPublisher {
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
    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(orderCreatedTopic, event.orderId(), event);
    }
}
