# EventCart

EventCart is a real-time e-commerce order platform built to learn modern Java backend development with Spring Boot, Kafka, MongoDB, and production-style engineering practices.

The goal is not only to build a working application, but also to understand the technologies deeply enough to explain design decisions in interviews.

## Current Technology Baseline

As of 2026-08-03, this project currently builds with:

| Area | Choice |
| --- | --- |
| Language | Java 25 |
| Backend framework | Spring Boot 4.1.x, Spring Framework 7.x |
| Messaging | Apache Kafka 4.3.x |
| Database | MongoDB 8.x |
| Cache | Redis |
| Security | Spring Security, OAuth2/JWT, Keycloak |
| Testing | JUnit, Mockito, AssertJ, Testcontainers |
| Observability | Spring Boot Actuator, Micrometer, Prometheus, Grafana, OpenTelemetry |
| Deployment | Docker, Docker Compose, Kubernetes-ready manifests |
| Documentation | Markdown docs, OpenAPI/Swagger |
| Frontend | React, TypeScript, Vite, React Router, React Query, Zustand, React Hook Form, Zod |

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
12. [Technology Deep Dives](docs/technology-deep-dives/README.md)
13. [Architecture Documentation](docs/architecture/README.md)
14. [Architecture Decision Records](docs/decisions/README.md)
15. [Interview Preparation](docs/interview/README.md)
16. [Frontend UI](frontend/eventcart-ui/README.md)

## Current Services

EventCart is developed as a Maven multi-module project:

| Service | Responsibility |
| --- | --- |
| API Gateway | Single entry point for client APIs, routing, JWT validation, and RBAC |
| Catalog Service | Products, categories, search, inventory-facing product metadata |
| Cart Service | Customer shopping cart, product snapshots, ownership checks, and internal cart cleanup |
| Order Service | Order placement, order snapshots, Redis idempotency, outbox event publishing, and inventory/payment-driven status updates |
| Inventory Service | Stock reservation, compensation, and outbox-backed inventory result events |
| Payment Service | Payment simulation and outbox-backed payment events |
| Notification Service | Async notification history plus optional email/SMS delivery from order and payment events |
| React UI | Browser-based operations console for catalog, cart, orders, inventory setup, and notifications |
| Common Libraries | Shared events, DTO conventions, exception models, security, Kafka retry/DLQ support, and test utilities |
| E2E Tests | Docker-backed full-platform tests that launch service jars and verify the order-to-notification flow |

## Core Business Flow

1. Customer browses products.
2. Customer adds products to cart through the React UI or API. Cart Service calls Catalog Service to fetch product details and stores a cart snapshot.
3. Customer places an order. Order Service calls Cart Service and stores an order snapshot.
4. Order Service stores `OrderCreatedEvent` in the MongoDB outbox.
5. Order Service outbox scheduler publishes `OrderCreatedEvent` to Kafka.
6. Inventory Service consumes `OrderCreated`, reserves stock, and stores `InventoryReserved` or `InventoryReservationFailed` in its outbox.
7. Inventory Service outbox scheduler publishes the inventory result event to Kafka.
8. Order Service consumes the inventory result, updates order status, and clears the cart after successful reservation.
9. Payment Service consumes `InventoryReserved`, simulates payment, and stores `PaymentCompleted` or `PaymentFailed` in its outbox.
10. Payment Service outbox scheduler publishes the payment result event to Kafka.
11. Order Service consumes payment result events and updates final payment/order status.
12. Inventory Service consumes failed payment events and releases reserved stock.
13. Notification Service stores customer notifications asynchronously and optionally delivers them through configured email/SMS providers.

## Learning Promise

By the end of this project, you should be able to:

- Build Spring Boot REST services from scratch.
- Design MongoDB document models and indexes.
- Produce and consume Kafka events safely.
- Explain eventual consistency, retries, dead-letter topics, idempotency, and the outbox pattern.
- Secure APIs with JWT and role-based access.
- Test services using Testcontainers.
- Write a full-platform E2E test that launches multiple services against Docker-backed infrastructure.
- Run the full stack locally with Docker Compose.
- Build a React UI that authenticates with Keycloak and calls the API Gateway.
- Discuss the project confidently in Java backend and microservices interviews.
