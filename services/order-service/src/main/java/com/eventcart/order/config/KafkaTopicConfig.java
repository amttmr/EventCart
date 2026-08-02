package com.eventcart.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic declarations owned by order-service.
 */
@Configuration
public class KafkaTopicConfig {
    /**
     * Declares the topic that carries newly created orders.
     *
     * @param topicName configured topic name
     * @return Kafka topic definition
     */
    @Bean
    public NewTopic orderCreatedTopic(@Value("${eventcart.kafka.topics.order-created}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
