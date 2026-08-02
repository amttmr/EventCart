package com.eventcart.notification.config;

import com.eventcart.common.kafka.KafkaDeadLetterSupport;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka topic declarations needed by notification-service retry recovery.
 */
@Configuration
public class KafkaTopicConfig {
    /**
     * Declares the DLQ for failed order-created notification processing.
     *
     * @param topicName source topic
     * @return DLQ topic definition
     */
    @Bean
    public NewTopic orderCreatedDeadLetterTopic(@Value("${eventcart.kafka.topics.order-created}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed inventory-failed notification processing.
     *
     * @param topicName source topic
     * @return DLQ topic definition
     */
    @Bean
    public NewTopic inventoryFailedDeadLetterTopic(@Value("${eventcart.kafka.topics.inventory-failed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed payment-completed notification processing.
     *
     * @param topicName source topic
     * @return DLQ topic definition
     */
    @Bean
    public NewTopic paymentCompletedDeadLetterTopic(@Value("${eventcart.kafka.topics.payment-completed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }

    /**
     * Declares the DLQ for failed payment-failed notification processing.
     *
     * @param topicName source topic
     * @return DLQ topic definition
     */
    @Bean
    public NewTopic paymentFailedDeadLetterTopic(@Value("${eventcart.kafka.topics.payment-failed}") String topicName) {
        return KafkaDeadLetterSupport.deadLetterTopic(topicName);
    }
}
