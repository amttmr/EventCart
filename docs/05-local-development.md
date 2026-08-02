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
  - Publish `OrderCreatedEvent` to Kafka.
  - Consume inventory reservation result events.
  - Update order status to `INVENTORY_RESERVED` or `INVENTORY_FAILED`.
  - Clear the cart after successful inventory reservation.
- `inventory-service`
  - Seed product stock for local testing.
  - Consume `OrderCreatedEvent` from Kafka.
  - Reserve stock when available.
  - Publish `InventoryReservedEvent` or `InventoryReservationFailedEvent`.
- Docker Compose:
  - MongoDB
  - Kafka
  - Redis for order idempotency keys
- OpenAPI/Swagger UI for service APIs.

## Important Java Note

The project currently targets Java 21 because your active terminal reports:

```text
java 21.0.8
```

After Java 25 is active in `JAVA_HOME` and `PATH`, we can update the root `pom.xml`:

```xml
<java.version>25</java.version>
```

## Start Infrastructure

From the project root:

```powershell
cd C:\Users\HP\Documents\Study\EventCart
docker compose up -d
```

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

When order-service places the order, it stores an idempotency key in Redis and publishes `OrderCreatedEvent` to Kafka topic `eventcart.orders.created`. inventory-service consumes that event asynchronously and creates a reservation result.

Check order status by order ID:

```powershell
Invoke-RestMethod "http://localhost:8083/api/v1/orders/<order-id>"
```

Check reservation result by order ID:

```powershell
Invoke-RestMethod "http://localhost:8084/api/v1/inventory/reservations/<order-id>"
```

After inventory is reserved, order-service consumes `InventoryReservedEvent`, updates the order status to `INVENTORY_RESERVED`, and calls cart-service to clear the cart. If stock is unavailable, order-service consumes `InventoryReservationFailedEvent`, updates the order status to `INVENTORY_FAILED`, and keeps the failure reason in `statusReason`.

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
- Kafka consumer basics with inventory reservation.
- Kafka consumer basics with order status updates from inventory result events.
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
15. What is the outbox pattern, and why will we need it later?
16. How does Redis-based idempotency protect order creation from duplicate client retries?
17. Why do we clear the cart after inventory reservation instead of immediately after order creation?
