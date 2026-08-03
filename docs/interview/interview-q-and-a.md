# Interview Q And A

## Project-Level Questions

| Question | Answer |
| --- | --- |
| What is EventCart? | A microservices-based e-commerce order platform built with Spring Boot, Kafka, MongoDB, Redis, Keycloak, observability tooling, Docker, CI/CD, and Kubernetes-ready manifests. |
| Why did you build it as microservices? | To learn service ownership, independent modules, synchronous and asynchronous communication, security boundaries, and production-style deployment patterns. |
| What is the main business flow? | Product browsing, cart creation, order placement, inventory reservation, payment simulation, order status update, and notification creation. |
| What is the most important learning from the project? | Real systems are not only CRUD. They need reliability, idempotency, observability, security, CI/CD, and failure handling. |

## Spring Boot Questions

| Question | Answer |
| --- | --- |
| Why Spring Boot? | It reduces boilerplate and gives production-ready support for web APIs, validation, configuration, Actuator, testing, security, MongoDB, Kafka, and Redis. |
| Controller vs service vs repository? | Controllers handle HTTP, services contain business logic, repositories handle persistence. |
| Why use DTOs? | DTOs keep API contracts separate from MongoDB documents and internal implementation details. |
| How is validation handled? | Request DTOs use validation annotations and exception handlers return consistent API errors. |

## MongoDB Questions

| Question | Answer |
| --- | --- |
| Why MongoDB? | Carts, orders, payments, and notifications are document-shaped and usually loaded as aggregates. |
| What is database per service? | Each service owns its own database and other services must not write directly into it. |
| Why store snapshots? | Cart and order history should reflect what the customer saw at that time, even if product details change later. |
| How are indexes used? | Frequently queried fields such as SKU, customer ID, status, and active flags are indexed. |

## Kafka Questions

| Question | Answer |
| --- | --- |
| Why Kafka? | It decouples order creation from inventory, payment, and notification processing. |
| What is eventual consistency? | Services update their own state at different times after events are processed, so the system becomes consistent after the workflow completes. |
| What is a consumer group? | A named group of consumers that share topic partitions so each message is processed by one consumer in that group. |
| Why do consumers need idempotency? | Kafka can deliver messages more than once, especially around retries and failures. |
| What is a DLQ? | A dead-letter topic stores messages that could not be processed after retries. |

## Redis Questions

| Question | Answer |
| --- | --- |
| Why Redis? | It provides fast atomic idempotency checks for order placement retries. |
| Is Redis the source of truth? | No. MongoDB stores orders. Redis stores temporary retry safety state. |
| Why use TTL? | Idempotency keys should not live forever. TTL limits storage growth. |

## Outbox Questions

| Question | Answer |
| --- | --- |
| What problem does outbox solve? | It reduces the dual-write risk between saving database state and publishing a Kafka event. |
| How does EventCart use outbox? | Order, inventory, and payment services store events in `outbox_events`, and schedulers publish pending records to Kafka. |
| Does outbox remove duplicate events? | No. It improves publishing reliability, but consumers must still handle duplicates. |

## Security Questions

| Question | Answer |
| --- | --- |
| Why Keycloak? | It gives a realistic OAuth2/OpenID Connect identity provider for local development. |
| What is a resource server? | An API that validates JWT access tokens before serving protected resources. |
| Why not secure only the gateway? | Backend services should not blindly trust network location. Defense in depth is safer. |
| What are ownership checks? | A customer token must match the requested customer resource, usually through the `customer_id` claim. |

## Observability Questions

| Question | Answer |
| --- | --- |
| Logs vs metrics vs traces? | Logs explain events, metrics show numeric behavior over time, traces show request flow across services. |
| Why correlation IDs? | They let developers connect logs from different services for the same request. |
| What does Prometheus do? | It scrapes metrics exposed by services and the OpenTelemetry Collector. |
| What does Grafana do? | It visualizes metrics in dashboards. |

## Failure Scenario Questions

| Question | Answer |
| --- | --- |
| What happens if inventory is insufficient? | Inventory-service publishes `InventoryReservationFailed`, order-service marks the order `INVENTORY_FAILED`, and notification-service stores a notification. |
| What happens if payment fails? | Payment-service publishes `PaymentFailed`, order-service marks the order `PAYMENT_FAILED`, and inventory-service releases reserved stock. |
| What happens if a Kafka listener keeps failing? | Spring Kafka retries the message and then publishes it to the matching `.dlq` topic. |
| What happens if the user retries order placement? | The same idempotency key returns the original order instead of creating a duplicate. |
