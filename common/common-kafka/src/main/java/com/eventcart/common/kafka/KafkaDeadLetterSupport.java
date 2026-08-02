package com.eventcart.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Factory methods for consistent Kafka retry and DLQ behavior.
 */
public final class KafkaDeadLetterSupport {
    /**
     * Suffix added to the original topic when a message is routed to a DLQ.
     */
    public static final String DLQ_SUFFIX = ".dlq";

    /**
     * Prevents creation of this utility class.
     */
    private KafkaDeadLetterSupport() {
    }

    /**
     * Creates a default error handler that retries and then publishes to a DLQ.
     *
     * @param kafkaTemplate Kafka template used to publish exhausted records
     * @param retryIntervalMs delay between retries in milliseconds
     * @param maxRetryAttempts number of retry attempts before DLQ publishing
     * @return configured Kafka error handler
     */
    public static DefaultErrorHandler defaultErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            long retryIntervalMs,
            long maxRetryAttempts
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + DLQ_SUFFIX, record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(retryIntervalMs, maxRetryAttempts));
    }

    /**
     * Creates a dead-letter topic definition for an existing business topic.
     *
     * @param sourceTopic source topic whose failed records should be preserved
     * @return DLQ topic definition
     */
    public static NewTopic deadLetterTopic(String sourceTopic) {
        return TopicBuilder.name(sourceTopic + DLQ_SUFFIX)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
