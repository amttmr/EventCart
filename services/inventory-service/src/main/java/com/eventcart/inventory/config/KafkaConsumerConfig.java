package com.eventcart.inventory.config;

import com.eventcart.common.events.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit Kafka consumer infrastructure for inventory-service.
 */
@Configuration
public class KafkaConsumerConfig {
    /**
     * Creates a consumer factory that deserializes order-created events.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId Kafka consumer group ID
     * @param autoOffsetReset offset reset strategy for new consumer groups
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        JacksonJsonDeserializer<OrderCreatedEvent> valueDeserializer = new JacksonJsonDeserializer<>(OrderCreatedEvent.class);
        valueDeserializer.addTrustedPackages("com.eventcart.common.events");
        valueDeserializer.setRemoveTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    /**
     * Creates the listener container factory used by {@code @KafkaListener}.
     *
     * @param consumerFactory order-created consumer factory
     * @return Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
