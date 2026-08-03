# Local Development

This document explains how to run the first working slice of EventCart locally.

## Current Build Status

Implemented so far:

- Maven parent project.
- Shared modules:
  - `common-events`
  - `common-web`
  - `common-test`
- `catalog-service`
  - Product creation.
  - Product lookup by ID.
  - Product search with filters.
  - Product update.
  - Product deactivation.
  - MongoDB document model and indexes.
  - Request validation and global error handling.
- `cart-service`
  - Customer cart lookup.
  - Add item to cart by product ID and quantity.
  - Fetch product details from catalog-service before saving a cart snapshot.
  - Update item quantity.
  - Remove item from cart.
  - Clear cart.
  - MongoDB embedded cart item model.
- `order-service`
  - Place order from cart.
  - Fetch cart details from cart-service.
  - Store order snapshot in MongoDB.
  - Use Redis idempotency keys for safe order-placement retries.
  - Store `OrderCreatedEvent` in the outbox.
  - Publish pending outbox events to Kafka.
  - Consume inventory reservation result events.
  - Update order status to `INVENTORY_RESERVED` or `INVENTORY_FAILED`.
  - Consume payment result events.
  - Update order status to `PAYMENT_COMPLETED` or `PAYMENT_FAILED`.
  - Clear the cart after successful inventory reservation.
- `inventory-service`
  - Seed product stock for local testing.
  - Consume `OrderCreatedEvent` from Kafka.
  - Reserve stock when available.
  - Consume `PaymentFailedEvent` from Kafka.
  - Release reserved stock when payment fails.
  - Store `InventoryReservedEvent` or `InventoryReservationFailedEvent` in the outbox.
  - Publish pending inventory outbox events to Kafka.
- `payment-service`
  - Consume `InventoryReservedEvent` from Kafka.
  - Simulate payment success or failure.
  - Store payment attempts in MongoDB.
  - Store `PaymentCompletedEvent` or `PaymentFailedEvent` in the outbox.
  - Publish pending payment outbox events to Kafka.
- `notification-service`
  - Consume order, inventory failure, and payment result events from Kafka.
  - Store customer notification history in MongoDB.
  - Optionally deliver notifications through SMTP email and Twilio-compatible SMS.
- `api-gateway`
  - Route `/api/v1/**` traffic to backend services.
  - Validate Keycloak JWT tokens and enforce roles.
- `common-security`
  - Enforce role-based access and customer ownership checks.
  - Allow a narrow internal token only for async cart cleanup after inventory reservation.
- Docker Compose:
  - MongoDB
  - Kafka
  - Redis for order idempotency keys
  - Keycloak
  - OpenTelemetry Collector
  - Prometheus
  - Grafana
- OpenAPI/Swagger UI for service APIs.
- Dockerfiles, GitHub Actions CI/CD workflow, Kubernetes manifests, and secret templates.
- Docker-backed end-to-end tests in `e2e-tests`.

## Important Java Note

The project targets Java 25. Before building or running services, confirm that both `java` and Maven use JDK 25:

```powershell
java -version
.\mvnw.cmd -version
```

Expected result:

```text
Java version: 25.x
```

The root `pom.xml` uses `<java.version>25</java.version>` and `maven.compiler.release` points to that value, so compiling the project requires a JDK 25 toolchain.

## Start Infrastructure

From the project root:

```powershell
cd C:\Users\HP\Documents\Study\EventCart
docker compose up -d
```

This starts MongoDB, Kafka, Redis, Keycloak, OpenTelemetry Collector, Prometheus, and Grafana.

Check containers:

```powershell
docker compose ps
```

Stop containers:

```powershell
docker compose down
```

Stop containers and remove local volumes:

```powershell
docker compose down -v
```

## Local Secrets And Optional Providers

The repository includes `.env.example` with local placeholders for infrastructure passwords, the internal service token, SMTP, and Twilio-compatible SMS credentials.

For normal local learning, the default internal service token in `application.yml` is enough for order-service to clear a cart after inventory reservation. For a production-like run, set a long random value:

```powershell
$env:EVENTCART_INTERNAL_SERVICE_TOKEN = "replace-with-a-long-random-token"
```

Email and SMS notifications are disabled by default. Enable them only after setting provider credentials:

```powershell
$env:SPRING_MAIL_HOST = "smtp.example.com"
$env:SPRING_MAIL_USERNAME = "smtp-user"
$env:SPRING_MAIL_PASSWORD = "smtp-password"
$env:TWILIO_ACCOUNT_SID = "replace-me"
$env:TWILIO_AUTH_TOKEN = "replace-me"
$env:TWILIO_FROM_NUMBER = "+15555550100"
```

Then set `eventcart.notifications.email.enabled=true` or `eventcart.notifications.sms.enabled=true` in `services/notification-service/src/main/resources/application.yml` or pass the equivalent environment-backed configuration for your run profile.

## Build

```powershell
mvn clean verify
```

If you changed a shared module such as `common-events` or `common-web`, install the reactor once before running a single service with Maven:

```powershell
mvn install -DskipTests
```

This updates your local Maven repository so `spring-boot:run` can see the latest shared classes.

Build only the catalog service and required modules:

```powershell
mvn -pl services/catalog-service -am clean verify
```

Build only the cart service and required modules:

```powershell
mvn -pl services/cart-service -am clean verify
```

Build only the order service and required modules:

```powershell
mvn -pl services/order-service -am clean verify
```

Build only the inventory service and required modules:

```powershell
mvn -pl services/inventory-service -am clean verify
```

Build only the payment service and required modules:

```powershell
mvn -pl services/payment-service -am clean verify
```

Build only the notification service and required modules:

```powershell
mvn -pl services/notification-service -am clean verify
```

Build only the API Gateway and required modules:

```powershell
mvn -pl services/api-gateway -am clean verify
```

Run the full Docker-backed platform E2E test:

```powershell
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
```

This test starts MongoDB, Kafka, and Redis with Testcontainers, launches the service jars, creates a product, seeds inventory, adds a cart item, places an order, and waits for inventory, payment, final order status, and notifications. Docker Desktop must be running; otherwise JUnit skips the Testcontainers test.

## Run Catalog Service

```powershell
mvn -pl services/catalog-service spring-boot:run
```

The service runs on:

```text
http://localhost:8081
```

Health endpoint:

```text
http://localhost:8081/actuator/health
```

Swagger/OpenAPI documentation:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

## Run Cart Service

Start catalog-service first because cart-service calls it when adding items:

```powershell
mvn -pl services/catalog-service spring-boot:run
```

Then run cart-service in another terminal:

```powershell
mvn -pl services/cart-service spring-boot:run
```

The service runs on:

```text
http://localhost:8082
```

Health endpoint:

```text
http://localhost:8082/actuator/health
```

Swagger/OpenAPI documentation:

```text
http://localhost:8082/swagger-ui.html
http://localhost:8082/v3/api-docs
```

## Run Order Service

Start cart-service first because order-service calls it when placing orders. Start Redis through Docker Compose because order-service stores idempotency keys there.

```powershell
mvn -pl services/order-service spring-boot:run
```

The service runs on:

```text
http://localhost:8083
```

Health endpoint:

```text
http://localhost:8083/actuator/health
```

Swagger/OpenAPI documentation:

```text
http://localhost:8083/swagger-ui.html
http://localhost:8083/v3/api-docs
```

## Run Inventory Service

Start Kafka through Docker Compose before starting inventory-service. Start inventory-service before placing an order if you want to watch the event get consumed immediately.

```powershell
mvn -pl services/inventory-service spring-boot:run
```

The service runs on:

```text
http://localhost:8084
```

Health endpoint:

```text
http://localhost:8084/actuator/health
```

Swagger/OpenAPI documentation:

```text
http://localhost:8084/swagger-ui.html
http://localhost:8084/v3/api-docs
```

## Run Payment Service

Start Kafka through Docker Compose before starting payment-service. Start payment-service before placing an order if you want to watch payment events get consumed immediately.

```powershell
mvn -pl services/payment-service spring-boot:run
```

The service runs on:

```text
http://localhost:8085
```

Health endpoint:

```text
http://localhost:8085/actuator/health
```

Swagger/OpenAPI documentation:

```text
http://localhost:8085/swagger-ui.html
http://localhost:8085/v3/api-docs
```

## Run Notification Service

Start Kafka through Docker Compose before starting notification-service.

```powershell
mvn -pl services/notification-service spring-boot:run
```

The service runs on:

```text
http://localhost:8086
```

Swagger/OpenAPI documentation:

```text
http://localhost:8086/swagger-ui.html
http://localhost:8086/v3/api-docs
```

## Run API Gateway

Start Keycloak through Docker Compose before testing secured gateway APIs.

```powershell
mvn -pl services/api-gateway spring-boot:run
```

The gateway runs on:

```text
http://localhost:8080
```

Use gateway paths such as:

```text
http://localhost:8080/api/v1/products
http://localhost:8080/api/v1/carts/customer-1
http://localhost:8080/api/v1/orders/customer/customer-1
```

## Get A Local Keycloak Token

Keycloak imports realm `eventcart` from `ops/keycloak/eventcart-realm.json`.

Admin token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8088/realms/eventcart/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=eventcart-gateway&username=admin-user&password=admin"

$adminToken = $tokenResponse.access_token
```

Customer token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8088/realms/eventcart/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=eventcart-gateway&username=customer-user&password=customer"

$customerToken = $tokenResponse.access_token
```

Call a secured gateway API:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/carts/customer-1" `
  -Headers @{ Authorization = "Bearer $customerToken" }
```

## Product API Examples

Create product:

```powershell
$body = @{
  sku = "SKU-1001"
  name = "Mechanical Keyboard"
  description = "Hot-swappable keyboard with RGB lighting"
  category = "Electronics"
  price = 6999.00
  currency = "INR"
  availableQuantity = 25
  tags = @("keyboard", "gaming", "rgb")
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8081/api/v1/products" `
  -ContentType "application/json" `
  -Body $body
```

Search products:

```powershell
Invoke-RestMethod "http://localhost:8081/api/v1/products?keyword=keyboard&category=Electronics&active=true"
```

Get product by ID:

```powershell
Invoke-RestMethod "http://localhost:8081/api/v1/products/<product-id>"
```

Update product:

```powershell
$body = @{
  name = "Mechanical Keyboard Pro"
  description = "Hot-swappable keyboard with RGB lighting"
  category = "Electronics"
  price = 7999.00
  currency = "INR"
  availableQuantity = 30
  tags = @("keyboard", "gaming", "rgb", "pro")
  active = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Put `
  -Uri "http://localhost:8081/api/v1/products/<product-id>" `
  -ContentType "application/json" `
  -Body $body
```

Deactivate product:

```powershell
Invoke-RestMethod -Method Delete "http://localhost:8081/api/v1/products/<product-id>"
```

## Cart API Examples

Get customer cart:

```powershell
Invoke-RestMethod "http://localhost:8082/api/v1/carts/customer-1"
```

Add item to cart:

```powershell
$body = @{
  productId = "<product-id>"
  quantity = 2
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8082/api/v1/carts/customer-1/items" `
  -ContentType "application/json" `
  -Body $body
```

When this request reaches cart-service, cart-service calls:

```text
GET http://localhost:8081/api/v1/products/<product-id>
```

Then it stores the product snapshot inside the `eventcart_cart.carts` MongoDB collection.

Update item quantity:

```powershell
$body = @{
  quantity = 3
} | ConvertTo-Json

Invoke-RestMethod -Method Put `
  -Uri "http://localhost:8082/api/v1/carts/customer-1/items/<product-id>" `
  -ContentType "application/json" `
  -Body $body
```

Remove item:

```powershell
Invoke-RestMethod -Method Delete "http://localhost:8082/api/v1/carts/customer-1/items/<product-id>"
```

Clear cart:

```powershell
Invoke-RestMethod -Method Delete "http://localhost:8082/api/v1/carts/customer-1"
```

## Inventory API Examples

Seed stock for the same product before placing an order:

```powershell
$body = @{
  sku = "SKU-1001"
  productName = "Mechanical Keyboard"
  availableQuantity = 25
} | ConvertTo-Json

Invoke-RestMethod -Method Put `
  -Uri "http://localhost:8084/api/v1/inventory/<product-id>" `
  -ContentType "application/json" `
  -Body $body
```

Get inventory item:

```powershell
Invoke-RestMethod "http://localhost:8084/api/v1/inventory/<product-id>"
```

## Order API Examples

Place order from the customer's cart:

```powershell
$body = @{
  customerId = "customer-1"
  idempotencyKey = "customer-1-order-20260802-001"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8083/api/v1/orders" `
  -ContentType "application/json" `
  -Body $body
```

When order-service places the order, it stores an idempotency key in Redis and stores `OrderCreatedEvent` in MongoDB collection `outbox_events`. The order outbox publisher sends the pending event to Kafka topic `eventcart.orders.created`. inventory-service consumes that event asynchronously, creates a reservation result, and stores the inventory result event in its own `outbox_events` collection. payment-service consumes successful inventory reservations, creates a payment attempt, and stores the payment result event in its own `outbox_events` collection before publishing it.

Check order status by order ID:

```powershell
Invoke-RestMethod "http://localhost:8083/api/v1/orders/<order-id>"
```

Check reservation result by order ID:

```powershell
Invoke-RestMethod "http://localhost:8084/api/v1/inventory/reservations/<order-id>"
```

Check payment attempt by order ID:

```powershell
Invoke-RestMethod "http://localhost:8085/api/v1/payments/orders/<order-id>"
```

After inventory is reserved, order-service consumes `InventoryReservedEvent`, updates the order status to `INVENTORY_RESERVED`, and calls cart-service to clear the cart. User-triggered cart reads forward the caller JWT; async cart cleanup uses the internal service token because the Kafka listener has no user HTTP request. payment-service then publishes `PaymentCompletedEvent` or `PaymentFailedEvent` through its outbox, and order-service updates the order status to `PAYMENT_COMPLETED` or `PAYMENT_FAILED`. If payment fails, inventory-service consumes `PaymentFailedEvent`, releases the reserved stock, and changes the reservation status to `RELEASED`. If stock is unavailable, order-service consumes `InventoryReservationFailedEvent`, updates the order status to `INVENTORY_FAILED`, and keeps the failure reason in `statusReason`.

## Observability URLs

After `docker compose up -d`, use these local observability tools:

| Tool | URL |
| --- | --- |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |
| OpenTelemetry OTLP HTTP endpoint | `http://localhost:4318` |

Grafana is provisioned with a Prometheus datasource and an EventCart overview dashboard. Prometheus scrapes the `/actuator/prometheus` endpoint of each locally running service.

## Docker, CI/CD, And Kubernetes

Each service has a Dockerfile under `services/<service>/Dockerfile`. Build from the repository root:

```powershell
docker build -f services/order-service/Dockerfile -t eventcart/order-service:local .
```

The GitHub Actions workflow at `.github/workflows/ci.yml` runs unit tests, the integration-test profile, service Docker builds, and GHCR image publishing for version tags.

Kubernetes manifests live in `ops/k8s`. They include namespace, non-secret ConfigMap values, an example Secret, service deployments, health probes, and secret references for MongoDB, notification providers, and the internal service token.

Payment simulation rule:

```text
Amounts below 50000.00 complete. Amounts at or above 50000.00 fail.
```

## What This Teaches

This first slice covers:

- Maven multi-module setup.
- Spring Boot application structure.
- REST controllers.
- DTO validation.
- Service layer.
- Repository layer.
- MongoDB documents.
- MongoDB indexes.
- Optimistic locking with `@Version`.
- Global exception handling.
- Docker Compose infrastructure.
- OpenAPI and Swagger UI.
- Embedded MongoDB document modeling for cart items.
- Synchronous service-to-service communication with Spring RestClient.
- Timeout handling and remote-service error mapping.
- Redis idempotency for safe order-placement retries.
- Kafka producer basics with `OrderCreatedEvent`.
- Transactional outbox basics with order-service, inventory-service, and payment-service `outbox_events`.
- Kafka consumer basics with inventory reservation.
- Kafka consumer basics with order status updates from inventory result events.
- Kafka event chaining with payment simulation after inventory reservation.
- Kafka retry and DLQ behavior with `<topic>.dlq`.
- Compensating actions by releasing reserved inventory after payment failure.
- JWT resource server basics with Keycloak.
- API Gateway routing and edge authorization.
- Customer ownership checks using JWT customer claims.
- Narrow internal service token handling for async backend calls.
- Correlation IDs, actuator metrics, Prometheus, and tracing basics.
- OpenTelemetry Collector, Prometheus, and Grafana local observability.
- Docker image creation, CI/CD workflow basics, Kubernetes manifests, and production-grade secret references.
- Full platform E2E testing with Testcontainers and bootable service jars.
- Event-driven workflow and eventual consistency.

## Spring Boot 4 MongoDB Configuration Note

Spring Boot 4 separates MongoDB connection settings from Spring Data MongoDB settings:

```yaml
spring:
  mongodb:
    uri: mongodb://eventcart:eventcart@localhost:27017/eventcart_catalog?authSource=admin
  data:
    mongodb:
      auto-index-creation: true
```

In older Spring Boot versions, many projects used `spring.data.mongodb.uri`. In Spring Boot 4, that key has been replaced by `spring.mongodb.uri`.

## Interview Questions From This Step

1. Why use a multi-module Maven project?
2. Why separate controller, service, repository, DTO, and domain packages?
3. Why is MongoDB a good fit for a product catalog?
4. What is the purpose of indexes in MongoDB?
5. What does optimistic locking solve?
6. Why should APIs use DTOs instead of database documents directly?
7. What is the purpose of Docker Compose in local development?
8. Why is a cart a good example for embedded MongoDB documents?
9. What problem does OpenAPI solve for REST APIs?
10. Why should cart-service fetch product details from catalog-service instead of trusting product price from the client?
11. What are the trade-offs of synchronous service-to-service calls?
12. Why does order-service store an order snapshot instead of reading from cart every time?
13. What problem does Kafka solve between order-service and inventory-service?
14. Why is idempotency important for Kafka consumers?
15. What is the outbox pattern, and why do order-service, inventory-service, and payment-service use it before publishing Kafka events?
16. How does Redis-based idempotency protect order creation from duplicate client retries?
17. Why do we clear the cart after inventory reservation instead of immediately after order creation?
18. Why does payment-service consume `InventoryReservedEvent` instead of `OrderCreatedEvent`?
19. How does payment-service handle duplicate Kafka messages?
20. Why does inventory-service need to release stock when payment fails?
21. Why should API Gateway authorization not be the only security layer?
22. How do retry and DLQ handling help Kafka consumers?
23. What does `X-Correlation-Id` solve in a microservices flow?
24. Why do customer ownership checks matter even after role-based access passes?
25. Why should real secrets come from environment variables, CI/CD secret stores, or Kubernetes Secret integrations instead of source code?
26. What do Prometheus, Grafana, and OpenTelemetry each contribute to observability?
27. What does a full platform E2E test catch that a unit test or mocked integration test cannot catch?
