package com.eventcart.common.events;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
        String eventId,
        String eventType,
        int eventVersion,
        String correlationId,
        Instant occurredAt
) {
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

