package com.eventcart.notification;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.test.TestProfiles;
import com.eventcart.notification.domain.NotificationType;
import com.eventcart.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kafka and MongoDB backed integration test for the order-payment-notification event flow.
 */
@SpringBootTest
@ActiveProfiles(TestProfiles.INTEGRATION_TEST)
@Testcontainers(disabledWithoutDocker = true)
class EventCartKafkaE2EIT {
    @Container
    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:3.8.0");

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${eventcart.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${eventcart.kafka.topics.payment-completed}")
    private String paymentCompletedTopic;

    /**
     * Registers container-backed infrastructure properties for the test context.
     *
     * @param registry dynamic property registry
     */
    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "notification-e2e-" + UUID.randomUUID());
        registry.add("eventcart.security.enabled", () -> "false");
        registry.add("eventcart.notifications.email.enabled", () -> "false");
        registry.add("eventcart.notifications.sms.enabled", () -> "false");
        registry.add("eventcart.kafka.retry.interval-ms", () -> "50");
        registry.add("eventcart.kafka.retry.max-attempts", () -> "1");
    }

    /**
     * Publishes order and payment events through real Kafka and verifies notification projection in real MongoDB.
     *
     * @throws Exception when Kafka publishing or polling fails
     */
    @Test
    void shouldProjectOrderAndPaymentEventsToCustomerNotifications() throws Exception {
        String orderId = "order-e2e-" + UUID.randomUUID();
        OrderCreatedEvent orderCreatedEvent = orderCreatedEvent(orderId);
        PaymentCompletedEvent paymentCompletedEvent = paymentCompletedEvent(orderId);

        kafkaTemplate.send(orderCreatedTopic, orderId, orderCreatedEvent).get(10, TimeUnit.SECONDS);
        kafkaTemplate.send(paymentCompletedTopic, orderId, paymentCompletedEvent).get(10, TimeUnit.SECONDS);
        waitForNotifications("customer-1", 2);

        var notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc("customer-1");

        assertThat(notifications).hasSize(2);
        assertThat(notifications).extracting("type")
                .contains(NotificationType.ORDER_CREATED, NotificationType.PAYMENT_COMPLETED);
        assertThat(notifications).allSatisfy(notification ->
                assertThat(notification.getCorrelationId()).isEqualTo(orderId));
    }

    /**
     * Waits until the notification projection reaches the expected size.
     *
     * @param customerId customer ID
     * @param expectedCount expected notification count
     * @throws InterruptedException when waiting is interrupted
     */
    private void waitForNotifications(String customerId, int expectedCount) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (System.nanoTime() < deadline) {
            if (notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).size() >= expectedCount) {
                return;
            }
            Thread.sleep(250);
        }
    }

    /**
     * Creates an order-created event for the integration flow.
     *
     * @param orderId order ID
     * @return order-created event
     */
    private OrderCreatedEvent orderCreatedEvent(String orderId) {
        return new OrderCreatedEvent(
                new EventMetadata("event-order-" + orderId, OrderCreatedEvent.EVENT_TYPE, OrderCreatedEvent.VERSION, orderId, java.time.Instant.now()),
                orderId,
                "customer-1",
                List.of(new OrderCreatedItem(
                        "product-1",
                        "SKU-1",
                        "Mechanical Keyboard",
                        new BigDecimal("6999.00"),
                        "INR",
                        1,
                        new BigDecimal("6999.00")
                )),
                new BigDecimal("6999.00"),
                "INR"
        );
    }

    /**
     * Creates a payment-completed event for the integration flow.
     *
     * @param orderId order ID
     * @return payment-completed event
     */
    private PaymentCompletedEvent paymentCompletedEvent(String orderId) {
        return new PaymentCompletedEvent(
                new EventMetadata("event-payment-" + orderId, PaymentCompletedEvent.EVENT_TYPE, PaymentCompletedEvent.VERSION, orderId, java.time.Instant.now()),
                "payment-" + orderId,
                orderId,
                "customer-1",
                new BigDecimal("6999.00"),
                "INR",
                "MockPay-" + orderId
        );
    }
}
