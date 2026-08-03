# Project Explanation Script

Use this document to practice explaining EventCart in interviews.

## 30-Second Version

EventCart is a real-time e-commerce order platform built with Java 25, Spring Boot, MongoDB, Kafka, Redis, Keycloak, Docker, Testcontainers, and observability tools. It has separate services for catalog, cart, order, inventory, payment, notification, and API gateway. The project demonstrates synchronous REST calls, asynchronous Kafka workflows, transactional outbox, retry/DLQ handling, Redis idempotency, JWT security, and full-platform testing.

## 2-Minute Version

EventCart models a realistic e-commerce checkout flow using microservices.

A customer uses the API Gateway as the single entry point. Catalog-service manages products in MongoDB. Cart-service lets the customer add products to a cart and calls catalog-service to store a product snapshot. When the customer places an order, order-service fetches the cart, stores an order snapshot, protects the request with a Redis idempotency key, and writes an `OrderCreated` event to a MongoDB outbox.

The outbox scheduler publishes the order event to Kafka. Inventory-service consumes it, reserves stock, and publishes either `InventoryReserved` or `InventoryReservationFailed`. If inventory is reserved, payment-service consumes the event, simulates payment, and publishes either `PaymentCompleted` or `PaymentFailed`. Order-service consumes inventory and payment events to update the final order status. Notification-service consumes the same events to create customer notifications.

The platform also has Keycloak-based JWT security, role and customer ownership checks, Kafka retry and DLQ handling, Testcontainers integration tests, Dockerfiles, GitHub Actions, Kubernetes manifests, and Prometheus/Grafana/OpenTelemetry observability.

## Deep-Dive Version

EventCart is designed around service ownership and eventual consistency.

Each service owns its database. This prevents direct cross-service database coupling. MongoDB is used because carts, orders, payments, and notifications are naturally document-shaped. For example, an order stores an item snapshot so historical orders do not change when catalog data changes later.

The checkout workflow uses Kafka because inventory, payment, and notification do not need to be completed in the same HTTP request. This makes the system more resilient and scalable, but it introduces eventual consistency. Because Kafka is at-least-once, consumers are written with idempotency in mind and failures are routed to DLQ topics after retry.

The transactional outbox pattern is used to reduce the dual-write problem between MongoDB and Kafka. Instead of directly publishing after saving business state, the service saves an outbox record and a scheduler publishes it. This gives us a recoverable place to inspect pending or failed events.

Security is implemented with Keycloak and Spring Security OAuth2 Resource Server. The gateway validates tokens and routes requests, but backend services also validate JWTs. Customer ownership checks use the `customer_id` claim so a customer cannot access another customer's cart, orders, or notifications.

The project is production-inspired rather than just CRUD. It includes CI/CD, Docker image builds, Kubernetes templates, Testcontainers tests, metrics, traces, logs, and correlation IDs.

## How To Explain The Flow On A Whiteboard

1. Draw the API Gateway first.
2. Draw six backend services: catalog, cart, order, inventory, payment, notification.
3. Draw MongoDB behind each service.
4. Draw Redis only behind order-service.
5. Draw Kafka between order, inventory, payment, notification, and order-service status updates.
6. Draw Keycloak connected to gateway and backend services.
7. Draw observability connected to all services.

## Strong Closing Line

"The project helped me learn not only Spring Boot APIs, but also real microservice concerns like service data ownership, eventual consistency, outbox reliability, idempotency, security, observability, CI/CD, and containerized testing."
