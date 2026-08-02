package com.eventcart.order.config;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * Explicit Kafka consumer infrastructure for inventory result events.
 */
@Configuration
public class KafkaConsumerConfig {
    /**
     * Creates a consumer factory for successful inventory reservation events.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId Kafka consumer group ID
     * @param autoOffsetReset offset reset strategy for new consumer groups
     * @return consumer factory for inventory-reserved events
     */
    @Bean
    public ConsumerFactory<String, InventoryReservedEvent> inventoryReservedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(bootstrapServers, groupId, autoOffsetReset),
                new StringDeserializer(),
                inventoryReservedDeserializer()
        );
    }

    /**
     * Creates a listener container factory for inventory-reserved events.
     *
     * @param consumerFactory consumer factory for inventory-reserved events
     * @return Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> inventoryReservedKafkaListenerContainerFactory(
            @Qualifier("inventoryReservedConsumerFactory") ConsumerFactory<String, InventoryReservedEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * Creates a consumer factory for failed inventory reservation events.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId Kafka consumer group ID
     * @param autoOffsetReset offset reset strategy for new consumer groups
     * @return consumer factory for inventory-failed events
     */
    @Bean
    public ConsumerFactory<String, InventoryReservationFailedEvent> inventoryFailedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(bootstrapServers, groupId, autoOffsetReset),
                new StringDeserializer(),
                inventoryFailedDeserializer()
        );
    }

    /**
     * Creates a listener container factory for inventory-failed events.
     *
     * @param consumerFactory consumer factory for inventory-failed events
     * @return Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> inventoryFailedKafkaListenerContainerFactory(
            @Qualifier("inventoryFailedConsumerFactory") ConsumerFactory<String, InventoryReservationFailedEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * Builds common Kafka consumer configuration properties.
     *
     * @param bootstrapServers Kafka bootstrap server list
     * @param groupId Kafka consumer group ID
     * @param autoOffsetReset offset reset strategy
     * @return Kafka consumer properties
     */
    private Map<String, Object> consumerProperties(String bootstrapServers, String groupId, String autoOffsetReset) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return properties;
    }

    /**
     * Creates a JSON deserializer for inventory-reserved events.
     *
     * @return configured event deserializer
     */
    private JacksonJsonDeserializer<InventoryReservedEvent> inventoryReservedDeserializer() {
        JacksonJsonDeserializer<InventoryReservedEvent> deserializer =
                new JacksonJsonDeserializer<>(InventoryReservedEvent.class);
        deserializer.addTrustedPackages("com.eventcart.common.events");
        deserializer.setRemoveTypeHeaders(false);
        return deserializer;
    }

    /**
     * Creates a JSON deserializer for inventory-failed events.
     *
     * @return configured event deserializer
     */
    private JacksonJsonDeserializer<InventoryReservationFailedEvent> inventoryFailedDeserializer() {
        JacksonJsonDeserializer<InventoryReservationFailedEvent> deserializer =
                new JacksonJsonDeserializer<>(InventoryReservationFailedEvent.class);
        deserializer.addTrustedPackages("com.eventcart.common.events");
        deserializer.setRemoveTypeHeaders(false);
        return deserializer;
    }
}
