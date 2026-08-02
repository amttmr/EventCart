package com.eventcart.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for shared EventCart JSON configuration.
 */
class EventCartJsonConfigurationTest {
    /**
     * Verifies that the fallback mapper can serialize and deserialize Java time
     * values used in EventCart event metadata and outbox payloads.
     *
     * @throws Exception when JSON serialization or deserialization fails
     */
    @Test
    void createsObjectMapperWithJavaTimeSupport() throws Exception {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(EventCartJsonConfiguration.class)) {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            Instant occurredAt = Instant.parse("2026-08-02T18:24:37Z");

            String json = objectMapper.writeValueAsString(new InstantPayload(occurredAt));
            InstantPayload restored = objectMapper.readValue(json, InstantPayload.class);

            assertThat(json).contains("2026-08-02T18:24:37Z");
            assertThat(restored.occurredAt()).isEqualTo(occurredAt);
        }
    }

    /**
     * Test payload used to prove Java time values round-trip through Jackson.
     *
     * @param occurredAt timestamp carried by the payload
     */
    private record InstantPayload(Instant occurredAt) {
    }
}
