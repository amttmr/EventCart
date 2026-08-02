package com.eventcart.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic declarations owned by payment-service.
 */
@Configuration
public class KafkaTopicConfig {
    /**
     * Declares the topic that carries completed payment events.
     *
     * @param topicName configured topic name
     * @return Kafka topic definition
     */
    @Bean
    public NewTopic paymentCompletedTopic(@Value("${eventcart.kafka.topics.payment-completed}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Declares the topic that carries failed payment events.
     *
     * @param topicName configured topic name
     * @return Kafka topic definition
     */
    @Bean
    public NewTopic paymentFailedTopic(@Value("${eventcart.kafka.topics.payment-failed}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
