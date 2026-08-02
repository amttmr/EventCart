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
  - Add item to cart.
  - Update item quantity.
  - Remove item from cart.
  - Clear cart.
  - MongoDB embedded cart item model.
- Docker Compose:
  - MongoDB
  - Kafka
  - Redis
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

Build only the catalog service and required modules:

```powershell
mvn -pl services/catalog-service -am clean verify
```

Build only the cart service and required modules:

```powershell
mvn -pl services/cart-service -am clean verify
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
  sku = "SKU-1001"
  productName = "Mechanical Keyboard"
  unitPrice = 6999.00
  currency = "INR"
  quantity = 2
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8082/api/v1/carts/customer-1/items" `
  -ContentType "application/json" `
  -Body $body
```

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
