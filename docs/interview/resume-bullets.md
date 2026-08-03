# Resume Bullets

Use these as starting points. Adjust wording based on your actual ownership and what you can confidently explain.

## Backend Development

- Built a Java 25 and Spring Boot microservices platform for real-time e-commerce order processing.
- Designed REST APIs for product catalog, shopping cart, order placement, inventory reservation, payment simulation, and notification history.
- Implemented request validation, global exception handling, OpenAPI documentation, and consistent API error responses.

## Microservices And Messaging

- Implemented Kafka-based asynchronous order workflow across order, inventory, payment, and notification services.
- Designed shared event contracts for `OrderCreated`, `InventoryReserved`, `InventoryReservationFailed`, `PaymentCompleted`, and `PaymentFailed`.
- Added Kafka retry and dead-letter-topic handling to preserve failed messages for debugging and recovery.

## Data And Reliability

- Modeled service-owned MongoDB databases for catalog, cart, order, inventory, payment, and notification domains.
- Implemented transactional outbox pattern for reliable event publishing from order, inventory, and payment services.
- Added Redis-backed idempotency for order placement to prevent duplicate checkout requests.

## Security

- Secured APIs with Keycloak, Spring Security OAuth2 Resource Server, JWT validation, and role-based access.
- Implemented customer ownership checks using token claims to prevent cross-customer data access.
- Added internal service token protection for narrow asynchronous service-to-service operations.

## Testing And DevOps

- Added unit, integration, and full-platform E2E tests using JUnit, Mockito, AssertJ, and Testcontainers.
- Created Dockerfiles for all services and a Docker Compose stack for MongoDB, Kafka, Redis, Keycloak, Prometheus, Grafana, and OpenTelemetry Collector.
- Built a GitHub Actions CI/CD workflow for Maven tests, Testcontainers integration tests, Docker image builds, and tagged GHCR image publishing.
- Prepared Kubernetes manifests and secret templates for production-style deployment.

## Observability

- Added Spring Boot Actuator, Micrometer metrics, Prometheus scraping, Grafana dashboards, OpenTelemetry tracing, and correlation IDs.
- Improved debugging of distributed workflows through structured logs, service health endpoints, Kafka DLQs, and database inspection.
