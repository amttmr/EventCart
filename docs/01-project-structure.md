# Project Structure

EventCart will use a Maven multi-module monorepo. This keeps all services in one repository while preserving real service boundaries.

## Target Directory Layout

```text
EventCart/
  README.md
  pom.xml
  mvnw
  mvnw.cmd
  compose.yaml
  .mvn/
  docker/
    kafka/
    mongodb/
    keycloak/
    prometheus/
    grafana/
  docs/
    00-prerequisites.md
    01-project-structure.md
    02-application-scope.md
    03-learning-and-interview-roadmap.md
    04-documentation-standards.md
    05-local-development.md
    06-api-documentation.md
    07-service-to-service-communication.md
    08-event-driven-order-inventory-flow.md
    09-logging-and-debugging.md
    api/
    architecture/
    decisions/
    interview/
  common/
    common-events/
    common-web/
    common-test/
  services/
    api-gateway/
    catalog-service/
    cart-service/
    order-service/
    inventory-service/
    payment-service/
    notification-service/
  ops/
    k8s/
    scripts/
  postman/
  bruno/
```

## Module Responsibilities

| Module | Responsibility |
| --- | --- |
| `common-events` | Shared Kafka event contracts and event metadata |
| `common-web` | Shared API response models, exception handling helpers, correlation ID support |
| `common-test` | Testcontainers setup, test data builders, reusable integration test utilities |
| `common-security` | Shared JWT resource server and Keycloak role mapping for backend services |
| `common-kafka` | Shared Kafka retry and dead-letter-topic helper |
| `api-gateway` | External API entry point, routing, security enforcement, request correlation |
| `catalog-service` | Product catalog, product search, categories, product availability view |
| `cart-service` | Customer cart operations |
| `order-service` | Order creation, order status, orchestration of order lifecycle |
| `inventory-service` | Stock management, reservation, release, inventory events |
| `payment-service` | Payment simulation, payment status, failure scenarios |
| `notification-service` | Async customer/admin notifications |

## Standard Service Layout

Each service will follow this internal package structure:

```text
src/main/java/com/eventcart/<service>/
  <Service>Application.java
  config/
  controller/
  service/
  repository/
  domain/
  dto/
  mapper/
  event/
  exception/
  security/

src/main/resources/
  application.yml
  application-local.yml
  application-test.yml

src/test/java/com/eventcart/<service>/
  unit/
  integration/
```

## Naming Conventions

| Type | Example |
| --- | --- |
| REST controller | `OrderController` |
| Service class | `OrderService` |
| Repository | `OrderRepository` |
| Mongo document | `OrderDocument` |
| Request DTO | `CreateOrderRequest` |
| Response DTO | `OrderResponse` |
| Kafka event | `OrderCreatedEvent` |
| Kafka listener | `OrderEventsListener` |
| Mapper | `OrderMapper` |

## Why Monorepo First

For learning and interview preparation, a monorepo is easier to manage because:

- One build command can validate all services.
- Shared event contracts are visible.
- Docker Compose can run the whole system locally.
- It is easier to explain service boundaries without managing many repositories.

Later, we can discuss when teams split services into separate repositories.
