package com.eventcart.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Docker-backed platform tests for happy-path and negative EventCart flows.
 */
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventCartPlatformE2EIT {
    private static final String INTERNAL_TOKEN = "e2e-internal-token";
    private static final String ORDER_CREATED_TOPIC = "eventcart.orders.created";
    private static final String INVENTORY_RESERVED_TOPIC = "eventcart.inventory.reserved";
    private static final String INVENTORY_FAILED_TOPIC = "eventcart.inventory.failed";
    private static final String PAYMENT_COMPLETED_TOPIC = "eventcart.payments.completed";
    private static final String PAYMENT_FAILED_TOPIC = "eventcart.payments.failed";
    private static final String DLQ_SUFFIX = ".dlq";
    private static final int TOPIC_PARTITIONS = 3;
    private static final short TOPIC_REPLICAS = 1;
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(120);
    private static final Duration FLOW_TIMEOUT = Duration.ofSeconds(45);
    private static final Duration DLQ_TIMEOUT = Duration.ofSeconds(20);

    @Container
    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:3.8.0");

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8.2-alpine")
            .withExposedPorts(6379);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private Path root;
    private ServicePorts ports;
    private ServiceProcessGroup services;

    /**
     * Starts all platform services once for the full E2E class.
     *
     * @throws Exception when infrastructure, Kafka topic creation, or service startup fails
     */
    @BeforeAll
    void startPlatform() throws Exception {
        root = repositoryRoot();
        Path logDir = root.resolve("e2e-tests").resolve("target").resolve("service-logs");
        Files.createDirectories(logDir);
        ports = ServicePorts.allocate();
        createKafkaTopics();
        services = new ServiceProcessGroup();

        try {
            services.start("catalog-service", serviceJar(root, "catalog-service"), ports.catalog(), logDir,
                    serviceArgs(ports.catalog(), "eventcart_catalog_e2e"));
            services.start("cart-service", serviceJar(root, "cart-service"), ports.cart(), logDir,
                    serviceArgs(ports.cart(), "eventcart_cart_e2e",
                            "--eventcart.clients.catalog.base-url=http://localhost:" + ports.catalog(),
                            "--eventcart.internal-service.token=" + INTERNAL_TOKEN));
            services.start("order-service", serviceJar(root, "order-service"), ports.order(), logDir,
                    serviceArgs(ports.order(), "eventcart_order_e2e",
                            "--spring.kafka.consumer.group-id=order-service-e2e",
                            "--eventcart.redis.host=" + REDIS.getHost(),
                            "--eventcart.redis.port=" + REDIS.getMappedPort(6379),
                            "--eventcart.clients.cart.base-url=http://localhost:" + ports.cart(),
                            "--eventcart.internal-service.token=" + INTERNAL_TOKEN,
                            "--eventcart.clients.cart.internal-token=" + INTERNAL_TOKEN));
            services.start("inventory-service", serviceJar(root, "inventory-service"), ports.inventory(), logDir,
                    serviceArgs(ports.inventory(), "eventcart_inventory_e2e",
                            "--spring.kafka.consumer.group-id=inventory-service-e2e"));
            services.start("payment-service", serviceJar(root, "payment-service"), ports.payment(), logDir,
                    serviceArgs(ports.payment(), "eventcart_payment_e2e",
                            "--spring.kafka.consumer.group-id=payment-service-e2e"));
            services.start("notification-service", serviceJar(root, "notification-service"), ports.notification(), logDir,
                    serviceArgs(ports.notification(), "eventcart_notification_e2e",
                            "--spring.kafka.consumer.group-id=notification-service-e2e",
                            "--eventcart.notifications.email.enabled=false",
                            "--eventcart.notifications.sms.enabled=false"));

            services.awaitHealthy("catalog-service", ports.catalog(), httpClient);
            services.awaitHealthy("cart-service", ports.cart(), httpClient);
            services.awaitHealthy("order-service", ports.order(), httpClient);
            services.awaitHealthy("inventory-service", ports.inventory(), httpClient);
            services.awaitHealthy("payment-service", ports.payment(), httpClient);
            services.awaitHealthy("notification-service", ports.notification(), httpClient);
        } catch (Exception | AssertionError ex) {
            services.close();
            throw ex;
        }
    }

    /**
     * Stops every service launched by the E2E suite.
     */
    @AfterAll
    void stopPlatform() {
        if (services != null) {
            services.close();
        }
    }

    /**
     * Drives the successful customer flow and verifies final order, inventory, payment, and notifications.
     *
     * @throws Exception when the HTTP flow or asynchronous assertions fail
     */
    @Test
    @Order(1)
    void shouldCompleteOrderInventoryPaymentAndNotificationFlow() throws Exception {
        String scenarioId = shortId();
        String customerId = "customer-happy-" + scenarioId;
        String sku = "SKU-E2E-HAPPY-" + scenarioId;
        String productName = "E2E Mechanical Keyboard " + scenarioId;

        String productId = createProduct(sku, productName, "6999.00", 25);
        seedInventory(productId, sku, productName, 25);
        addItemToCart(customerId, productId, 2);
        String orderId = placeOrder(customerId, "idempotency-happy-" + scenarioId);

        JsonNode finalOrder = waitForOrderStatus(orderId, "PAYMENT_COMPLETED");
        assertThat(finalOrder.path("data").path("customerId").asText()).isEqualTo(customerId);

        JsonNode reservation = getJson("http://localhost:" + ports.inventory()
                + "/api/v1/inventory/reservations/" + orderId);
        assertThat(reservation.path("data").path("status").asText()).isEqualTo("RESERVED");

        JsonNode payment = getJson("http://localhost:" + ports.payment()
                + "/api/v1/payments/orders/" + orderId);
        assertThat(payment.path("data").path("status").asText()).isEqualTo("COMPLETED");

        JsonNode notifications = waitForNotifications(customerId, "ORDER_CREATED", "PAYMENT_COMPLETED");
        assertThat(notifications.path("data")).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Verifies that order-service rejects an order request when the customer's cart is empty.
     *
     * @throws Exception when the HTTP flow or assertions fail
     */
    @Test
    @Order(2)
    void shouldRejectOrderPlacementWhenCartIsEmpty() throws Exception {
        String scenarioId = shortId();
        String customerId = "customer-empty-" + scenarioId;

        JsonNode response = postJsonExpectingStatus(
                "http://localhost:" + ports.order() + "/api/v1/orders",
                orderRequest(customerId, "idempotency-empty-" + scenarioId),
                409
        );

        assertThat(response.path("code").asText()).isEqualTo("EMPTY_CART");
        assertThat(response.path("message").asText()).contains(customerId);

        JsonNode orders = getJson("http://localhost:" + ports.order()
                + "/api/v1/orders/customer/" + customerId);
        assertThat(orders.path("data")).isEmpty();
    }

    /**
     * Verifies that insufficient stock moves the order to INVENTORY_FAILED and does not create a payment.
     *
     * @throws Exception when the HTTP flow or asynchronous assertions fail
     */
    @Test
    @Order(3)
    void shouldFailOrderWhenInventoryIsInsufficient() throws Exception {
        String scenarioId = shortId();
        String customerId = "customer-inventory-fail-" + scenarioId;
        String sku = "SKU-E2E-LOW-STOCK-" + scenarioId;
        String productName = "E2E Low Stock Product " + scenarioId;

        String productId = createProduct(sku, productName, "1299.00", 25);
        seedInventory(productId, sku, productName, 1);
        addItemToCart(customerId, productId, 2);
        String orderId = placeOrder(customerId, "idempotency-inventory-fail-" + scenarioId);

        JsonNode failedOrder = waitForOrderStatus(orderId, "INVENTORY_FAILED");
        assertThat(failedOrder.path("data").path("statusReason").asText()).contains("Insufficient stock");

        JsonNode reservation = getJson("http://localhost:" + ports.inventory()
                + "/api/v1/inventory/reservations/" + orderId);
        assertThat(reservation.path("data").path("status").asText()).isEqualTo("FAILED");
        assertThat(reservation.path("data").path("failureReason").asText()).contains("Insufficient stock");

        JsonNode missingPayment = getJsonExpectingStatus(
                "http://localhost:" + ports.payment() + "/api/v1/payments/orders/" + orderId,
                404
        );
        assertThat(missingPayment.path("code").asText()).isEqualTo("PAYMENT_ATTEMPT_NOT_FOUND");

        JsonNode cart = getJson("http://localhost:" + ports.cart() + "/api/v1/carts/" + customerId);
        assertThat(cart.path("data").path("totalItems").asInt()).isEqualTo(2);

        JsonNode notifications = waitForNotifications(customerId, "ORDER_CREATED", "INVENTORY_FAILED");
        assertThat(notifications.path("data")).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Verifies that mock payment decline updates the order and releases reserved stock.
     *
     * @throws Exception when the HTTP flow or asynchronous assertions fail
     */
    @Test
    @Order(4)
    void shouldFailPaymentAndReleaseInventoryWhenAmountCrossesThreshold() throws Exception {
        String scenarioId = shortId();
        String customerId = "customer-payment-fail-" + scenarioId;
        String sku = "SKU-E2E-PAYMENT-FAIL-" + scenarioId;
        String productName = "E2E Premium Monitor " + scenarioId;

        String productId = createProduct(sku, productName, "30000.00", 10);
        seedInventory(productId, sku, productName, 5);
        addItemToCart(customerId, productId, 2);
        String orderId = placeOrder(customerId, "idempotency-payment-fail-" + scenarioId);

        JsonNode failedOrder = waitForOrderStatus(orderId, "PAYMENT_FAILED");
        assertThat(failedOrder.path("data").path("statusReason").asText()).contains("Mock payment declined");

        JsonNode payment = getJson("http://localhost:" + ports.payment()
                + "/api/v1/payments/orders/" + orderId);
        assertThat(payment.path("data").path("status").asText()).isEqualTo("FAILED");
        assertThat(payment.path("data").path("failureReason").asText()).contains("Mock payment declined");

        JsonNode releasedReservation = waitForJson(
                () -> getJson("http://localhost:" + ports.inventory()
                        + "/api/v1/inventory/reservations/" + orderId),
                json -> "RELEASED".equals(json.path("data").path("status").asText()),
                FLOW_TIMEOUT,
                "inventory reservation to be released after payment failure"
        );
        assertThat(releasedReservation.path("data").path("failureReason").asText())
                .contains("Released after payment failure");

        JsonNode stock = getJson("http://localhost:" + ports.inventory() + "/api/v1/inventory/" + productId);
        assertThat(stock.path("data").path("availableQuantity").asInt()).isEqualTo(5);
        assertThat(stock.path("data").path("reservedQuantity").asInt()).isZero();

        JsonNode notifications = waitForNotifications(customerId, "ORDER_CREATED", "PAYMENT_FAILED");
        assertThat(notifications.path("data")).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Verifies that reusing a completed idempotency key returns the existing order instead of creating another one.
     *
     * @throws Exception when the HTTP flow or asynchronous assertions fail
     */
    @Test
    @Order(5)
    void shouldReturnExistingOrderWhenIdempotencyKeyIsReused() throws Exception {
        String scenarioId = shortId();
        String customerId = "customer-idempotency-" + scenarioId;
        String sku = "SKU-E2E-IDEMPOTENCY-" + scenarioId;
        String productName = "E2E Idempotency Mouse " + scenarioId;
        String idempotencyKey = "idempotency-duplicate-" + scenarioId;

        String productId = createProduct(sku, productName, "2499.00", 10);
        seedInventory(productId, sku, productName, 5);
        addItemToCart(customerId, productId, 1);

        JsonNode firstResponse = placeOrderResponse(customerId, idempotencyKey);
        JsonNode duplicateResponse = placeOrderResponse(customerId, idempotencyKey);

        String firstOrderId = firstResponse.path("data").path("orderId").asText();
        String duplicateOrderId = duplicateResponse.path("data").path("orderId").asText();
        assertThat(duplicateOrderId).isEqualTo(firstOrderId);

        JsonNode orders = getJson("http://localhost:" + ports.order()
                + "/api/v1/orders/customer/" + customerId);
        assertThat(orders.path("data")).hasSize(1);

        JsonNode finalOrder = waitForOrderStatus(firstOrderId, "PAYMENT_COMPLETED");
        assertThat(finalOrder.path("data").path("orderId").asText()).isEqualTo(firstOrderId);
    }

    /**
     * Verifies that a listener failure is retried and then published to the order-created DLQ.
     *
     * @throws Exception when Kafka publishing, consuming, or assertions fail
     */
    @Test
    @Order(6)
    void shouldRoutePoisonKafkaMessageToDeadLetterTopicAfterRetry() throws Exception {
        String poisonOrderId = "poison-order-" + shortId();

        publishPoisonOrderCreatedEvent(poisonOrderId);
        ConsumerRecord<String, String> dlqRecord = waitForDlqRecord(ORDER_CREATED_TOPIC + DLQ_SUFFIX, poisonOrderId);

        assertThat(dlqRecord.topic()).isEqualTo(ORDER_CREATED_TOPIC + DLQ_SUFFIX);
        assertThat(dlqRecord.key()).isEqualTo(poisonOrderId);

        JsonNode payload = objectMapper.readTree(dlqRecord.value());
        assertThat(payload.path("orderId").asText()).isEqualTo(poisonOrderId);
        assertThat(payload.get("metadata").isNull()).isTrue();
    }

    /**
     * Creates a product through catalog-service.
     *
     * @param sku product SKU
     * @param productName product display name
     * @param price product unit price
     * @param catalogAvailableQuantity catalog-facing available quantity
     * @return product ID returned by catalog-service
     * @throws Exception when the HTTP call fails
     */
    private String createProduct(
            String sku,
            String productName,
            String price,
            int catalogAvailableQuantity
    ) throws Exception {
        JsonNode response = postJson("http://localhost:" + ports.catalog() + "/api/v1/products", """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "Product used by the full platform E2E test",
                  "category": "Electronics",
                  "price": %s,
                  "currency": "INR",
                  "availableQuantity": %d,
                  "tags": ["e2e"]
                }
                """.formatted(sku, productName, price, catalogAvailableQuantity));
        return response.path("data").path("id").asText();
    }

    /**
     * Seeds inventory for one product.
     *
     * @param productId product ID to seed
     * @param sku product SKU
     * @param productName product display name
     * @param availableQuantity stock available for reservation
     * @throws Exception when the HTTP call fails
     */
    private void seedInventory(
            String productId,
            String sku,
            String productName,
            int availableQuantity
    ) throws Exception {
        putJson("http://localhost:" + ports.inventory() + "/api/v1/inventory/" + productId, """
                {
                  "sku": "%s",
                  "productName": "%s",
                  "availableQuantity": %d
                }
                """.formatted(sku, productName, availableQuantity));
    }

    /**
     * Adds one product to the customer cart through cart-service.
     *
     * @param customerId customer ID
     * @param productId product ID to add
     * @param quantity quantity to add
     * @throws Exception when the HTTP call fails
     */
    private void addItemToCart(String customerId, String productId, int quantity) throws Exception {
        postJson("http://localhost:" + ports.cart() + "/api/v1/carts/" + customerId + "/items", """
                {
                  "productId": "%s",
                  "quantity": %d
                }
                """.formatted(productId, quantity));
    }

    /**
     * Places an order through order-service.
     *
     * @param customerId customer whose cart should become an order
     * @param idempotencyKey idempotency key for safe retries
     * @return order ID returned by order-service
     * @throws Exception when the HTTP call fails
     */
    private String placeOrder(String customerId, String idempotencyKey) throws Exception {
        return placeOrderResponse(customerId, idempotencyKey)
                .path("data")
                .path("orderId")
                .asText();
    }

    /**
     * Places an order and returns the full API response.
     *
     * @param customerId customer whose cart should become an order
     * @param idempotencyKey idempotency key for safe retries
     * @return parsed order API response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode placeOrderResponse(String customerId, String idempotencyKey) throws Exception {
        return postJson("http://localhost:" + ports.order() + "/api/v1/orders",
                orderRequest(customerId, idempotencyKey));
    }

    /**
     * Builds the order placement request body.
     *
     * @param customerId customer whose cart should become an order
     * @param idempotencyKey idempotency key for safe retries
     * @return JSON request body
     */
    private String orderRequest(String customerId, String idempotencyKey) {
        return """
                {
                  "customerId": "%s",
                  "idempotencyKey": "%s"
                }
                """.formatted(customerId, idempotencyKey);
    }

    /**
     * Waits until an order reaches a specific status.
     *
     * @param orderId order ID
     * @param status expected status
     * @return final order API response
     * @throws InterruptedException when waiting is interrupted
     */
    private JsonNode waitForOrderStatus(String orderId, String status) throws InterruptedException {
        return waitForJson(
                () -> getJson("http://localhost:" + ports.order() + "/api/v1/orders/" + orderId),
                json -> status.equals(json.path("data").path("status").asText()),
                FLOW_TIMEOUT,
                "order " + orderId + " to reach " + status
        );
    }

    /**
     * Waits until a customer has all requested notification types.
     *
     * @param customerId customer ID
     * @param types notification types that must be present
     * @return notification list API response
     * @throws InterruptedException when waiting is interrupted
     */
    private JsonNode waitForNotifications(String customerId, String... types) throws InterruptedException {
        return waitForJson(
                () -> getJson("http://localhost:" + ports.notification()
                        + "/api/v1/notifications/customers/" + customerId),
                json -> hasNotificationTypes(json, types),
                FLOW_TIMEOUT,
                "notifications " + String.join(", ", types) + " for " + customerId
        );
    }

    /**
     * Publishes a syntactically valid order-created event that fails inside listeners.
     *
     * @param orderId poison order ID used as Kafka key
     * @throws Exception when Kafka publishing fails
     */
    private void publishPoisonOrderCreatedEvent(String orderId) throws Exception {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(
                kafkaProducerProperties(),
                new StringSerializer(),
                new StringSerializer()
        )) {
            producer.send(new ProducerRecord<>(ORDER_CREATED_TOPIC, orderId, """
                    {
                      "metadata": null,
                      "orderId": "%s",
                      "customerId": "customer-dlq",
                      "items": [],
                      "totalAmount": 1.00,
                      "currency": "INR"
                    }
                    """.formatted(orderId))).get(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Waits for a Kafka record with the expected key to appear on a DLQ topic.
     *
     * @param topic DLQ topic name
     * @param expectedKey expected Kafka record key
     * @return matching DLQ record
     * @throws InterruptedException when waiting is interrupted
     */
    private ConsumerRecord<String, String> waitForDlqRecord(
            String topic,
            String expectedKey
    ) throws InterruptedException {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                kafkaConsumerProperties("e2e-dlq-" + shortId()),
                new StringDeserializer(),
                new StringDeserializer()
        )) {
            consumer.subscribe(List.of(topic));
            long deadline = System.nanoTime() + DLQ_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (expectedKey.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        throw new AssertionError("Timed out waiting for Kafka DLQ record key=" + expectedKey + " topic=" + topic);
    }

    /**
     * Builds Kafka producer properties for E2E helper publishing.
     *
     * @return Kafka producer properties
     */
    private Map<String, Object> kafkaProducerProperties() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()
        );
    }

    /**
     * Builds Kafka consumer properties for E2E helper consuming.
     *
     * @param groupId consumer group ID
     * @return Kafka consumer properties
     */
    private Map<String, Object> kafkaConsumerProperties(String groupId) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
    }

    /**
     * Builds the common command-line arguments used by every service process.
     *
     * @param port HTTP port for the service
     * @param databaseName MongoDB database name
     * @param extraArgs service-specific arguments
     * @return command-line arguments
     */
    private List<String> serviceArgs(int port, String databaseName, String... extraArgs) {
        List<String> args = new ArrayList<>(List.of(
                "--server.port=" + port,
                "--spring.main.banner-mode=off",
                "--eventcart.security.enabled=false",
                "--spring.mongodb.uri=" + mongoUri(databaseName),
                "--spring.data.mongodb.auto-index-creation=true",
                "--spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                "--spring.kafka.consumer.auto-offset-reset=earliest",
                "--eventcart.kafka.retry.interval-ms=100",
                "--eventcart.kafka.retry.max-attempts=1",
                "--eventcart.kafka.topics.order-created=" + ORDER_CREATED_TOPIC,
                "--eventcart.kafka.topics.inventory-reserved=" + INVENTORY_RESERVED_TOPIC,
                "--eventcart.kafka.topics.inventory-failed=" + INVENTORY_FAILED_TOPIC,
                "--eventcart.kafka.topics.payment-completed=" + PAYMENT_COMPLETED_TOPIC,
                "--eventcart.kafka.topics.payment-failed=" + PAYMENT_FAILED_TOPIC,
                "--eventcart.outbox.initial-delay=1s",
                "--eventcart.outbox.poll-delay=1s",
                "--management.tracing.sampling.probability=0.0"
        ));
        args.addAll(List.of(extraArgs));
        return args;
    }

    /**
     * Converts the Testcontainers Mongo replica set URL to a service-specific database URL.
     *
     * @param databaseName database name
     * @return MongoDB connection string
     */
    private String mongoUri(String databaseName) {
        String uri = MONGO.getReplicaSetUrl();
        if (uri.contains("/test?")) {
            return uri.replace("/test?", "/" + databaseName + "?");
        }
        return uri.replace("/test", "/" + databaseName);
    }

    /**
     * Creates all platform topics before service consumers subscribe.
     *
     * <p>Without this pre-step Kafka may auto-create a topic with the broker default partition count
     * when the first listener starts. Later services then try to increase the partition count, and
     * already-running consumers can miss keyed records assigned to the new partitions during the E2E run.</p>
     *
     * @throws Exception when topic creation fails
     */
    private void createKafkaTopics() throws Exception {
        try (AdminClient adminClient = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()
        ))) {
            adminClient.createTopics(List.of(
                    topic(ORDER_CREATED_TOPIC),
                    topic(INVENTORY_RESERVED_TOPIC),
                    topic(INVENTORY_FAILED_TOPIC),
                    topic(PAYMENT_COMPLETED_TOPIC),
                    topic(PAYMENT_FAILED_TOPIC),
                    topic(ORDER_CREATED_TOPIC + DLQ_SUFFIX),
                    topic(INVENTORY_RESERVED_TOPIC + DLQ_SUFFIX),
                    topic(INVENTORY_FAILED_TOPIC + DLQ_SUFFIX),
                    topic(PAYMENT_COMPLETED_TOPIC + DLQ_SUFFIX),
                    topic(PAYMENT_FAILED_TOPIC + DLQ_SUFFIX)
            )).all().get(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Creates a Kafka topic definition using the E2E partition and replica settings.
     *
     * @param topicName topic name
     * @return topic definition
     */
    private NewTopic topic(String topicName) {
        return new NewTopic(topicName, TOPIC_PARTITIONS, TOPIC_REPLICAS);
    }

    /**
     * Reads JSON from one URL.
     *
     * @param url URL to call
     * @return parsed JSON response
     */
    private JsonNode getJson(String url) {
        return getJsonExpectingSuccess(url);
    }

    /**
     * Reads JSON and asserts a 2xx HTTP status.
     *
     * @param url URL to call
     * @return parsed JSON response
     */
    private JsonNode getJsonExpectingSuccess(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).as("HTTP status for GET " + url).isBetween(200, 299);
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new IllegalStateException("HTTP GET failed: " + url, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP GET interrupted: " + url, ex);
        }
    }

    /**
     * Reads JSON and asserts an exact HTTP status.
     *
     * @param url URL to call
     * @param expectedStatus expected HTTP status code
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode getJsonExpectingStatus(String url, int expectedStatus) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("HTTP status for GET " + url).isEqualTo(expectedStatus);
        return objectMapper.readTree(response.body());
    }

    /**
     * Posts JSON to one URL.
     *
     * @param url URL to call
     * @param body JSON body
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode postJson(String url, String body) throws Exception {
        return sendJson("POST", url, body);
    }

    /**
     * Posts JSON and asserts an exact HTTP status.
     *
     * @param url URL to call
     * @param body JSON body
     * @param expectedStatus expected HTTP status code
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode postJsonExpectingStatus(String url, String body, int expectedStatus) throws Exception {
        return sendJsonExpectingStatus("POST", url, body, expectedStatus);
    }

    /**
     * Puts JSON to one URL.
     *
     * @param url URL to call
     * @param body JSON body
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode putJson(String url, String body) throws Exception {
        return sendJson("PUT", url, body);
    }

    /**
     * Sends a JSON request and asserts a 2xx response.
     *
     * @param method HTTP method
     * @param url URL to call
     * @param body JSON body
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode sendJson(String method, String url, String body) throws Exception {
        HttpResponse<String> response = sendJsonRequest(method, url, body);
        assertThat(response.statusCode()).as("HTTP status for " + method + " " + url).isBetween(200, 299);
        return objectMapper.readTree(response.body());
    }

    /**
     * Sends a JSON request and asserts an exact response status.
     *
     * @param method HTTP method
     * @param url URL to call
     * @param body JSON body
     * @param expectedStatus expected HTTP status code
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode sendJsonExpectingStatus(
            String method,
            String url,
            String body,
            int expectedStatus
    ) throws Exception {
        HttpResponse<String> response = sendJsonRequest(method, url, body);
        assertThat(response.statusCode()).as("HTTP status for " + method + " " + url).isEqualTo(expectedStatus);
        return objectMapper.readTree(response.body());
    }

    /**
     * Sends one JSON HTTP request.
     *
     * @param method HTTP method
     * @param url URL to call
     * @param body JSON body
     * @return HTTP response
     * @throws Exception when the HTTP call fails
     */
    private HttpResponse<String> sendJsonRequest(String method, String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Polls a JSON supplier until the expected condition is true.
     *
     * @param supplier JSON supplier
     * @param predicate assertion predicate
     * @param timeout maximum wait time
     * @param description description used in assertion failures
     * @return the matching JSON payload
     * @throws InterruptedException when waiting is interrupted
     */
    private JsonNode waitForJson(
            Supplier<JsonNode> supplier,
            java.util.function.Predicate<JsonNode> predicate,
            Duration timeout,
            String description
    ) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        JsonNode last = objectMapper.createObjectNode();
        while (System.nanoTime() < deadline) {
            last = supplier.get();
            if (predicate.test(last)) {
                return last;
            }
            TimeUnit.MILLISECONDS.sleep(500);
        }
        throw new AssertionError("Timed out waiting for " + description + ". Last response: " + last);
    }

    /**
     * Checks whether a notification list contains all requested notification types.
     *
     * @param response notification list API response
     * @param types expected notification types
     * @return true when the response contains every type
     */
    private boolean hasNotificationTypes(JsonNode response, String... types) {
        for (String type : types) {
            if (!hasNotificationType(response, type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a notification list contains a specific notification type.
     *
     * @param response notification list API response
     * @param type expected notification type
     * @return true when the response contains the type
     */
    private boolean hasNotificationType(JsonNode response, String type) {
        for (JsonNode notification : response.path("data")) {
            if (type.equals(notification.path("type").asText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a short unique identifier for E2E data.
     *
     * @return eight-character unique suffix
     */
    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Resolves the repository root from the Maven module working directory.
     *
     * @return repository root path
     */
    private Path repositoryRoot() {
        Path userDir = Path.of(System.getProperty("user.dir"));
        if (Files.exists(userDir.resolve("services"))) {
            return userDir;
        }
        return userDir.getParent();
    }

    /**
     * Resolves a bootable service jar path.
     *
     * @param root repository root
     * @param serviceName service module name
     * @return service jar path
     */
    private Path serviceJar(Path root, String serviceName) {
        return root.resolve("services")
                .resolve(serviceName)
                .resolve("target")
                .resolve(serviceName + "-0.1.0-SNAPSHOT.jar");
    }

    /**
     * Allocated local service ports.
     *
     * @param catalog catalog-service port
     * @param cart cart-service port
     * @param order order-service port
     * @param inventory inventory-service port
     * @param payment payment-service port
     * @param notification notification-service port
     */
    private record ServicePorts(int catalog, int cart, int order, int inventory, int payment, int notification) {
        /**
         * Allocates currently free local ports for service processes.
         *
         * @return allocated ports
         * @throws IOException when a port cannot be allocated
         */
        private static ServicePorts allocate() throws IOException {
            Set<Integer> ports = new LinkedHashSet<>();
            while (ports.size() < 6) {
                ports.add(freePort());
            }
            List<Integer> allocated = new ArrayList<>(ports);
            return new ServicePorts(
                    allocated.get(0),
                    allocated.get(1),
                    allocated.get(2),
                    allocated.get(3),
                    allocated.get(4),
                    allocated.get(5)
            );
        }

        /**
         * Reserves a free port and returns its number.
         *
         * @return free port number
         * @throws IOException when a port cannot be reserved
         */
        private static int freePort() throws IOException {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            }
        }
    }

    /**
     * Tracks service processes and shuts them down after the test class.
     */
    private static final class ServiceProcessGroup implements AutoCloseable {
        private final List<ManagedService> services = new ArrayList<>();

        /**
         * Starts one service process.
         *
         * @param serviceName service name
         * @param jarPath bootable jar path
         * @param port HTTP port
         * @param logDir directory for service logs
         * @param args Spring Boot arguments
         * @throws IOException when the process cannot be started
         */
        private void start(String serviceName, Path jarPath, int port, Path logDir, List<String> args) throws IOException {
            assertThat(Files.exists(jarPath)).as("Bootable jar for " + serviceName).isTrue();
            Path logFile = logDir.resolve(serviceName + ".log");
            List<String> command = new ArrayList<>();
            command.add(Path.of(System.getProperty("java.home")).resolve("bin").resolve("java").toString());
            command.add("-jar");
            command.add(jarPath.toString());
            command.addAll(args);

            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(logFile.toFile())
                    .start();
            services.add(new ManagedService(serviceName, process, logFile, port));
        }

        /**
         * Waits until one service reports actuator health UP.
         *
         * @param serviceName service name
         * @param port service port
         * @param httpClient HTTP client
         * @throws Exception when the service fails to become healthy
         */
        private void awaitHealthy(String serviceName, int port, HttpClient httpClient) throws Exception {
            ManagedService service = services.stream()
                    .filter(candidate -> candidate.name().equals(serviceName))
                    .findFirst()
                    .orElseThrow();
            URI healthUri = URI.create("http://localhost:" + port + "/actuator/health");
            long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                if (!service.process().isAlive()) {
                    throw new AssertionError(serviceName + " exited before it became healthy. Log tail:\n"
                            + service.tailLog());
                }
                try {
                    HttpRequest request = HttpRequest.newBuilder(healthUri)
                            .GET()
                            .timeout(Duration.ofSeconds(3))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 && response.body().contains("\"UP\"")) {
                        return;
                    }
                } catch (IOException ignored) {
                    // Service is still starting; keep polling.
                }
                TimeUnit.MILLISECONDS.sleep(500);
            }
            throw new AssertionError(serviceName + " did not become healthy. Log tail:\n" + service.tailLog());
        }

        /**
         * Stops every service process.
         */
        @Override
        public void close() {
            for (ManagedService service : services.reversed()) {
                service.process().destroy();
                try {
                    if (!service.process().waitFor(5, TimeUnit.SECONDS)) {
                        service.process().destroyForcibly();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    service.process().destroyForcibly();
                }
            }
        }
    }

    /**
     * One managed service process.
     *
     * @param name service name
     * @param process Java process
     * @param logFile captured log file
     * @param port HTTP port
     */
    private record ManagedService(String name, Process process, Path logFile, int port) {
        /**
         * Reads the last portion of the service log.
         *
         * @return service log tail
         */
        private String tailLog() {
            try {
                String log = Files.readString(logFile);
                return log.substring(Math.max(0, log.length() - 4000));
            } catch (IOException ex) {
                return "Unable to read log file: " + logFile;
            }
        }
    }
}
