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
  .dockerignore
  .env.example
  .github/
    workflows/
      ci.yml
  .mvn/
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
    common-security/
    common-kafka/
  services/
    api-gateway/
    catalog-service/
    cart-service/
    order-service/
    inventory-service/
    payment-service/
    notification-service/
  e2e-tests/
  ops/
    docker/
    k8s/
    keycloak/
    observability/
      otel/
      prometheus/
      grafana/
  postman/
  bruno/
```

## Module Responsibilities

| Module | Responsibility |
| --- | --- |
| `common-events` | Shared Kafka event contracts and event metadata |
| `common-web` | Shared API response models, exception handling helpers, correlation ID support |
| `common-test` | Testcontainers dependencies, test profile constants, reusable integration test utilities |
| `common-security` | Shared JWT resource server, Keycloak role mapping, customer ownership checks, and narrow internal service-token support |
| `common-kafka` | Shared Kafka retry and dead-letter-topic helper |
| `api-gateway` | External API entry point, routing, security enforcement, request correlation |
| `catalog-service` | Product catalog, product search, categories, product availability view |
| `cart-service` | Customer cart operations |
| `order-service` | Order creation, Redis idempotency, order outbox, order status updates, lifecycle orchestration |
| `inventory-service` | Stock management, reservation, release, inventory outbox events |
| `payment-service` | Payment simulation, payment status, payment outbox events |
| `notification-service` | Async customer notifications, notification history, optional email/SMS delivery |
| `e2e-tests` | Docker-backed end-to-end tests that launch service jars and exercise the full platform flow |

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
  outbox/
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
