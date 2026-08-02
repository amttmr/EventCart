package com.eventcart.order;

import com.eventcart.order.outbox.OrderOutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Context regression tests for order-service application wiring.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "eventcart.security.enabled=false",
                "eventcart.outbox.initial-delay=1h",
                "eventcart.outbox.poll-delay=1h",
                "spring.data.mongodb.auto-index-creation=false",
                "spring.kafka.admin.auto-create=false",
                "spring.kafka.listener.auto-startup=false"
        }
)
@ActiveProfiles("test")
class OrderServiceApplicationContextTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderOutboxService orderOutboxService;

    /**
     * Verifies that the service context provides the JSON mapper required by
     * transactional outbox components.
     */
    @Test
    void contextLoadsWithObjectMapperForOutbox() {
        assertThat(objectMapper).isNotNull();
        assertThat(orderOutboxService).isNotNull();
    }
}
