package com.eventcart.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Docker-backed platform test for the order-to-inventory-to-payment-to-notification flow.
 */
@Testcontainers(disabledWithoutDocker = true)
class EventCartPlatformE2EIT {
    private static final String INTERNAL_TOKEN = "e2e-internal-token";
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(120);
    private static final Duration FLOW_TIMEOUT = Duration.ofSeconds(45);

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

    /**
     * Starts all service jars, drives the customer HTTP flow, and verifies asynchronous outcomes.
     *
     * @throws Exception when a service process, HTTP call, or assertion fails
     */
    @Test
    void shouldCompleteOrderInventoryPaymentAndNotificationFlow() throws Exception {
        Path root = repositoryRoot();
        Path logDir = root.resolve("e2e-tests").resolve("target").resolve("service-logs");
        Files.createDirectories(logDir);
        ServicePorts ports = ServicePorts.allocate();
        createKafkaTopics();

        try (ServiceProcessGroup services = new ServiceProcessGroup()) {
            services.start("catalog-service", serviceJar(root, "catalog-service"), ports.catalog(), logDir,
                    serviceArgs(ports.catalog(), "eventcart_catalog_e2e"));
            services.start("cart-service", serviceJar(root, "cart-service"), ports.cart(), logDir,
                    serviceArgs(ports.cart(), "eventcart_cart_e2e",
                            "--eventcart.clients.catalog.base-url=http://localhost:" + ports.catalog(),
                            "--eventcart.internal-service.token=" + INTERNAL_TOKEN));
            services.start("order-service", serviceJar(root, "order-service"), ports.order(), logDir,
                    serviceArgs(ports.order(), "eventcart_order_e2e",
                            "--eventcart.redis.host=" + REDIS.getHost(),
                            "--eventcart.redis.port=" + REDIS.getMappedPort(6379),
                            "--eventcart.clients.cart.base-url=http://localhost:" + ports.cart(),
                            "--eventcart.internal-service.token=" + INTERNAL_TOKEN,
                            "--eventcart.clients.cart.internal-token=" + INTERNAL_TOKEN));
            services.start("inventory-service", serviceJar(root, "inventory-service"), ports.inventory(), logDir,
                    serviceArgs(ports.inventory(), "eventcart_inventory_e2e"));
            services.start("payment-service", serviceJar(root, "payment-service"), ports.payment(), logDir,
                    serviceArgs(ports.payment(), "eventcart_payment_e2e"));
            services.start("notification-service", serviceJar(root, "notification-service"), ports.notification(), logDir,
                    serviceArgs(ports.notification(), "eventcart_notification_e2e",
                            "--eventcart.notifications.email.enabled=false",
                            "--eventcart.notifications.sms.enabled=false"));

            services.awaitHealthy("catalog-service", ports.catalog(), httpClient);
            services.awaitHealthy("cart-service", ports.cart(), httpClient);
            services.awaitHealthy("order-service", ports.order(), httpClient);
            services.awaitHealthy("inventory-service", ports.inventory(), httpClient);
            services.awaitHealthy("payment-service", ports.payment(), httpClient);
            services.awaitHealthy("notification-service", ports.notification(), httpClient);

            String productId = createProduct(ports.catalog());
            seedInventory(ports.inventory(), productId);
            addItemToCart(ports.cart(), productId);
            String orderId = placeOrder(ports.order());

            JsonNode finalOrder = waitForJson(
                    () -> getJson("http://localhost:" + ports.order() + "/api/v1/orders/" + orderId),
                    json -> "PAYMENT_COMPLETED".equals(json.path("data").path("status").asText()),
                    FLOW_TIMEOUT,
                    "order to reach PAYMENT_COMPLETED"
            );
            assertThat(finalOrder.path("data").path("customerId").asText()).isEqualTo("customer-1");

            JsonNode reservation = getJson("http://localhost:" + ports.inventory()
                    + "/api/v1/inventory/reservations/" + orderId);
            assertThat(reservation.path("data").path("status").asText()).isEqualTo("RESERVED");

            JsonNode payment = getJson("http://localhost:" + ports.payment()
                    + "/api/v1/payments/orders/" + orderId);
            assertThat(payment.path("data").path("status").asText()).isEqualTo("COMPLETED");

            JsonNode notifications = waitForJson(
                    () -> getJson("http://localhost:" + ports.notification()
                            + "/api/v1/notifications/customers/customer-1"),
                    json -> hasNotificationType(json, "ORDER_CREATED") && hasNotificationType(json, "PAYMENT_COMPLETED"),
                    FLOW_TIMEOUT,
                    "order and payment notifications"
            );
            assertThat(notifications.path("data")).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    /**
     * Creates a product through catalog-service.
     *
     * @param port catalog-service port
     * @return product ID returned by catalog-service
     * @throws Exception when the HTTP call fails
     */
    private String createProduct(int port) throws Exception {
        JsonNode response = postJson("http://localhost:" + port + "/api/v1/products", """
                {
                  "sku": "SKU-E2E-1001",
                  "name": "E2E Mechanical Keyboard",
                  "description": "Keyboard used by the full platform E2E test",
                  "category": "Electronics",
                  "price": 6999.00,
                  "currency": "INR",
                  "availableQuantity": 25,
                  "tags": ["keyboard", "e2e"]
                }
                """);
        return response.path("data").path("id").asText();
    }

    /**
     * Seeds inventory for one product.
     *
     * @param port inventory-service port
     * @param productId product ID to seed
     * @throws Exception when the HTTP call fails
     */
    private void seedInventory(int port, String productId) throws Exception {
        putJson("http://localhost:" + port + "/api/v1/inventory/" + productId, """
                {
                  "sku": "SKU-E2E-1001",
                  "productName": "E2E Mechanical Keyboard",
                  "availableQuantity": 25
                }
                """);
    }

    /**
     * Adds one product to the customer cart through cart-service.
     *
     * @param port cart-service port
     * @param productId product ID to add
     * @throws Exception when the HTTP call fails
     */
    private void addItemToCart(int port, String productId) throws Exception {
        postJson("http://localhost:" + port + "/api/v1/carts/customer-1/items", """
                {
                  "productId": "%s",
                  "quantity": 2
                }
                """.formatted(productId));
    }

    /**
     * Places an order through order-service.
     *
     * @param port order-service port
     * @return order ID returned by order-service
     * @throws Exception when the HTTP call fails
     */
    private String placeOrder(int port) throws Exception {
        JsonNode response = postJson("http://localhost:" + port + "/api/v1/orders", """
                {
                  "customerId": "customer-1",
                  "idempotencyKey": "customer-1-e2e-%s"
                }
                """.formatted(UUID.randomUUID()));
        return response.path("data").path("orderId").asText();
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
                    new NewTopic("eventcart.orders.created", 3, (short) 1),
                    new NewTopic("eventcart.inventory.reserved", 3, (short) 1),
                    new NewTopic("eventcart.inventory.failed", 3, (short) 1),
                    new NewTopic("eventcart.payments.completed", 3, (short) 1),
                    new NewTopic("eventcart.payments.failed", 3, (short) 1),
                    new NewTopic("eventcart.orders.created.DLT", 3, (short) 1),
                    new NewTopic("eventcart.inventory.reserved.DLT", 3, (short) 1),
                    new NewTopic("eventcart.inventory.failed.DLT", 3, (short) 1),
                    new NewTopic("eventcart.payments.completed.DLT", 3, (short) 1),
                    new NewTopic("eventcart.payments.failed.DLT", 3, (short) 1)
            )).all().get(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Reads JSON from one URL.
     *
     * @param url URL to call
     * @return parsed JSON response
     */
    private JsonNode getJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isBetween(200, 299);
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new IllegalStateException("HTTP GET failed: " + url, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP GET interrupted: " + url, ex);
        }
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
     * Sends a JSON request and parses the wrapped EventCart API response.
     *
     * @param method HTTP method
     * @param url URL to call
     * @param body JSON body
     * @return parsed JSON response
     * @throws Exception when the HTTP call fails
     */
    private JsonNode sendJson(String method, String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isBetween(200, 299);
        return objectMapper.readTree(response.body());
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
            return new ServicePorts(freePort(), freePort(), freePort(), freePort(), freePort(), freePort());
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
     * Tracks service processes and shuts them down after the test.
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
