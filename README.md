# EventCart

EventCart is a real-time e-commerce order platform built to learn modern Java backend development with Spring Boot, Kafka, MongoDB, and production-style engineering practices.

The goal is not only to build a working application, but also to understand the technologies deeply enough to explain design decisions in interviews.

## Current Technology Baseline

As of 2026-08-02, this project currently builds with:

| Area | Choice |
| --- | --- |
| Language | Java 21, with an upgrade path to Java 25 after the local JDK is changed |
| Backend framework | Spring Boot 4.1.x, Spring Framework 7.x |
| Messaging | Apache Kafka 4.3.x |
| Database | MongoDB 8.x |
| Cache | Redis |
| Security | Spring Security, OAuth2/JWT, Keycloak |
| Testing | JUnit, Mockito, AssertJ, Testcontainers |
| Observability | Spring Boot Actuator, Micrometer, Prometheus, Grafana, OpenTelemetry |
| Deployment | Docker, Docker Compose, Kubernetes-ready manifests |
| Documentation | Markdown docs, OpenAPI/Swagger |

## Documentation Index

Start here:

1. [Prerequisites](docs/00-prerequisites.md)
2. [Project Structure](docs/01-project-structure.md)
3. [Application Scope](docs/02-application-scope.md)
4. [Learning and Interview Roadmap](docs/03-learning-and-interview-roadmap.md)
5. [Documentation Standards](docs/04-documentation-standards.md)
6. [Local Development](docs/05-local-development.md)
7. [API Documentation](docs/06-api-documentation.md)
8. [Service-to-Service Communication](docs/07-service-to-service-communication.md)
9. [Event-Driven Order and Inventory Flow](docs/08-event-driven-order-inventory-flow.md)
10. [Logging and Debugging](docs/09-logging-and-debugging.md)
11. [QA and New Joiner Application Flow Guide](docs/10-qa-application-flow.md)

## Planned Services

EventCart will be developed as a Maven multi-module project:

| Service | Responsibility |
| --- | --- |
| API Gateway | Single entry point for client APIs |
| Catalog Service | Products, categories, search, inventory-facing product metadata |
| Cart Service | Customer shopping cart |
| Order Service | Order placement, order snapshots, Redis idempotency, and inventory-driven status updates |
| Inventory Service | Stock reservation and inventory reservation result events |
| Payment Service | Payment simulation and payment events |
| Notification Service | Email/SMS-style async notifications |
| Common Libraries | Shared events, DTO conventions, exception models, test utilities |

## Core Business Flow

1. Customer browses products.
2. Customer adds products to cart. Cart Service calls Catalog Service to fetch product details and stores a cart snapshot.
3. Customer places an order. Order Service calls Cart Service and stores an order snapshot.
4. Order Service publishes `OrderCreated`.
5. Inventory Service consumes `OrderCreated`, reserves stock, and publishes `InventoryReserved` or `InventoryReservationFailed`.
6. Order Service consumes the inventory result, updates order status, and clears the cart after successful reservation.
7. Payment Service processes payment and publishes `PaymentCompleted` or `PaymentFailed`.
8. Order Service updates final payment/order status.
9. Notification Service sends customer updates asynchronously.

## Learning Promise

By the end of this project, you should be able to:

- Build Spring Boot REST services from scratch.
- Design MongoDB document models and indexes.
- Produce and consume Kafka events safely.
- Explain eventual consistency, retries, dead-letter topics, idempotency, and the outbox pattern.
- Secure APIs with JWT and role-based access.
- Test services using Testcontainers.
- Run the full stack locally with Docker Compose.
- Discuss the project confidently in Java backend and microservices interviews.
