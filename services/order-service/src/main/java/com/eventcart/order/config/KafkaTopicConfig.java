package com.eventcart.order.config;

import com.eventcart.common.kafka.KafkaDeadLetterSupport;
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

    /**
     * Declares the DLQ for failed inventory-reserved processing.
     *
     * @param topicName inventory-reserved topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic inventoryReservedDeadLetterTopic(@Value("${eventcart.kafka.topics.inventory-reserved}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed inventory-failed processing.
     *
     * @param topicName inventory-failed topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic inventoryFailedDeadLetterTopic(@Value("${eventcart.kafka.topics.inventory-failed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed payment-completed processing.
     *
     * @param topicName payment-completed topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic paymentCompletedDeadLetterTopic(@Value("${eventcart.kafka.topics.payment-completed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed payment-failed processing.
     *
     * @param topicName payment-failed topic
     * @return Kafka dead-letter topic definition
     */
    @Bean
    public NewTopic paymentFailedDeadLetterTopic(@Value("${eventcart.kafka.topics.payment-failed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }
}
