package com.eventcart.inventory.config;

import com.eventcart.common.kafka.KafkaDeadLetterSupport;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic declarations owned by inventory-service.
 */
@Configuration
public class KafkaTopicConfig {
    /**
     * Declares the topic that carries successful inventory reservations.
     *
     * @param topicName configured topic name
     * @return Kafka topic definition
     */
    @Bean
    public NewTopic inventoryReservedTopic(@Value("${eventcart.kafka.topics.inventory-reserved}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Declares the topic that carries failed inventory reservations.
     *
     * @param topicName configured topic name
     * @return Kafka topic definition
     */
    @Bean
    public NewTopic inventoryFailedTopic(@Value("${eventcart.kafka.topics.inventory-failed}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Declares the DLQ for failed order-created processing.
     *
     * @param topicName order-created topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic orderCreatedDeadLetterTopic(@Value("${eventcart.kafka.topics.order-created}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed payment-failed compensation processing.
     *
     * @param topicName payment-failed topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic paymentFailedDeadLetterTopic(@Value("${eventcart.kafka.topics.payment-failed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }
}
