package com.eventcart.notification.config;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.common.kafka.KafkaDeadLetterSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer infrastructure for notification-service event projections.
 */
@Configuration
public class KafkaConsumerConfig {
    /**
     * Creates an order-created consumer factory.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerFactory(bootstrapServers, groupId, autoOffsetReset, OrderCreatedEvent.class);
    }

    /**
     * Creates an inventory-failed consumer factory.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, InventoryReservationFailedEvent> inventoryFailedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerFactory(bootstrapServers, groupId, autoOffsetReset, InventoryReservationFailedEvent.class);
    }

    /**
     * Creates a payment-completed consumer factory.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerFactory(bootstrapServers, groupId, autoOffsetReset, PaymentCompletedEvent.class);
    }

    /**
     * Creates a payment-failed consumer factory.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerFactory(bootstrapServers, groupId, autoOffsetReset, PaymentFailedEvent.class);
    }

    /**
     * Creates an order-created listener container factory.
     *
     * @param consumerFactory consumer factory
     * @param kafkaErrorHandler retry and DLQ handler
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedKafkaListenerContainerFactory(
            @Qualifier("orderCreatedConsumerFactory") ConsumerFactory<String, OrderCreatedEvent> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        return listenerFactory(consumerFactory, kafkaErrorHandler);
    }

    /**
     * Creates an inventory-failed listener container factory.
     *
     * @param consumerFactory consumer factory
     * @param kafkaErrorHandler retry and DLQ handler
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> inventoryFailedKafkaListenerContainerFactory(
            @Qualifier("inventoryFailedConsumerFactory") ConsumerFactory<String, InventoryReservationFailedEvent> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        return listenerFactory(consumerFactory, kafkaErrorHandler);
    }

    /**
     * Creates a payment-completed listener container factory.
     *
     * @param consumerFactory consumer factory
     * @param kafkaErrorHandler retry and DLQ handler
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedKafkaListenerContainerFactory(
            @Qualifier("paymentCompletedConsumerFactory") ConsumerFactory<String, PaymentCompletedEvent> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        return listenerFactory(consumerFactory, kafkaErrorHandler);
    }

    /**
     * Creates a payment-failed listener container factory.
     *
     * @param consumerFactory consumer factory
     * @param kafkaErrorHandler retry and DLQ handler
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedKafkaListenerContainerFactory(
            @Qualifier("paymentFailedConsumerFactory") ConsumerFactory<String, PaymentFailedEvent> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        return listenerFactory(consumerFactory, kafkaErrorHandler);
    }

    /**
     * Creates the retry and DLQ handler used by notification consumers.
     *
     * @param kafkaTemplate Kafka template used by the recoverer
     * @param retryIntervalMs delay between retry attempts
     * @param maxRetryAttempts retries before DLQ publishing
     * @return Kafka error handler
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${eventcart.kafka.retry.interval-ms:1000}") long retryIntervalMs,
            @Value("${eventcart.kafka.retry.max-attempts:3}") long maxRetryAttempts
    ) {
        return KafkaDeadLetterSupport.defaultErrorHandler(kafkaTemplate, retryIntervalMs, maxRetryAttempts);
    }

    /**
     * Creates a typed Kafka consumer factory.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @param eventType expected event type
     * @param <T> event type
     * @return consumer factory
     */
    private <T> ConsumerFactory<String, T> consumerFactory(
            String bootstrapServers,
            String groupId,
            String autoOffsetReset,
            Class<T> eventType
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        JacksonJsonDeserializer<T> valueDeserializer = new JacksonJsonDeserializer<>(eventType);
        valueDeserializer.addTrustedPackages("com.eventcart.common.events");
        valueDeserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
    }

    /**
     * Creates a listener container factory with the common error handler.
     *
     * @param consumerFactory typed consumer factory
     * @param kafkaErrorHandler retry and DLQ handler
     * @param <T> event type
     * @return listener container factory
     */
    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(
            ConsumerFactory<String, T> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
