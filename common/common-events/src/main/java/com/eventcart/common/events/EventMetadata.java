package com.eventcart.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Common metadata that travels with every EventCart domain event.
 *
 * <p>The metadata makes asynchronous Kafka messages traceable and safer to
 * process. The event ID can be used for idempotency, the event type identifies
 * the business event, the version supports schema evolution, and the
 * correlation ID connects the event back to the original user request.</p>
 *
 * @param eventId unique ID for this specific event instance
 * @param eventType stable event type name, such as {@code catalog.product.created}
 * @param eventVersion version of the event payload schema
 * @param correlationId ID used to trace one business flow across services
 * @param occurredAt timestamp when the event was created
 */
public record EventMetadata(
        String eventId,
        String eventType,
        int eventVersion,
        String correlationId,
        Instant occurredAt
) {
    /**
     * Creates metadata for a new event instance.
     *
     * @param eventType stable event type name
     * @param eventVersion version of the event payload schema
     * @param correlationId request or workflow correlation ID
     * @return metadata populated with a generated event ID and current timestamp
     */
    public static EventMetadata create(String eventType, int eventVersion, String correlationId) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                eventType,
                eventVersion,
                correlationId,
                Instant.now()
        );
    }
}
