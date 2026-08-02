package com.eventcart.inventory.config;

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
}
