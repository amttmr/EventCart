# Application Scope

This document defines what EventCart will do, who uses it, and how the main workflows behave.

## User Roles

| Role | Capabilities |
| --- | --- |
| Customer | Browse products, manage cart, place orders, view order status |
| Admin | Create products, update inventory, view orders, manage catalog |
| System | Process events, reserve stock, simulate payments, send notifications |

## Core Features

### Catalog

- Create, update, delete, and list products.
- Assign products to categories.
- Search products by name, category, tags, and price range.
- Store product documents in MongoDB.
- Add indexes for common search fields.

### Cart

- Create a cart for a customer.
- Add item to cart.
- Update item quantity.
- Remove item from cart.
- Clear cart after successful order placement.
- Enforce customer ownership so one customer cannot operate another customer's cart.
- Allow a narrowly scoped internal service token for asynchronous order-service cart cleanup.

### Order

- Place an order from cart.
- Store order snapshot so product price/name changes do not corrupt old orders.
- Store `OrderCreatedEvent` in the transactional outbox before Kafka publishing.
- Use Redis idempotency keys to make client retries safe.
- Track order status:
  - `CREATED`
  - `INVENTORY_RESERVED`
  - `INVENTORY_FAILED`
  - `PAYMENT_COMPLETED`
  - `PAYMENT_FAILED`
  - Future: `CONFIRMED`, `CANCELLED`
- Expose APIs to view order details and order history.

### Inventory

- Maintain available stock per product.
- Reserve stock when an order is created.
- Release stock when payment fails or order is cancelled.
- Store inventory success/failure events in the transactional outbox before Kafka publishing.
- Demonstrate concurrency and optimistic locking.

### Payment

- Simulate payment processing.
- Support success and failure scenarios.
- Store payment events in the transactional outbox before Kafka publishing.
- Demonstrate idempotent event consumption, retries, and dead-letter topics.

### Notification

- Consume order, inventory, and payment events.
- Generate customer notifications.
- Store notification history in MongoDB.
- Optionally deliver notifications through SMTP email and Twilio-compatible SMS when provider credentials are configured.

## Main User Flow

1. Admin creates products and inventory.
2. Customer logs in.
3. Customer browses catalog.
4. Customer adds items to cart.
5. Customer places order.
6. Order Service creates order with `CREATED` status.
7. Order Service stores `OrderCreatedEvent` in the outbox.
8. Order Service outbox publisher sends `OrderCreatedEvent` to Kafka.
9. Inventory Service consumes the event and reserves stock.
10. Inventory Service stores the reservation result event in its outbox.
11. Inventory Service outbox publisher sends the result event to Kafka.
12. Payment Service processes payment after stock reservation.
13. Payment Service stores the payment result event in its outbox.
14. Payment Service outbox publisher sends the result event to Kafka.
15. Order Service updates order status from later events.
16. Notification Service stores order updates and optionally delivers email/SMS messages.
17. Customer checks order status.

## Kafka Topics

| Topic | Producer | Consumers |
| --- | --- | --- |
| `eventcart.orders.created` | Order Service outbox publisher | Inventory Service, Notification Service |
| `eventcart.inventory.reserved` | Inventory Service outbox publisher | Order Service, Payment Service |
| `eventcart.inventory.failed` | Inventory Service outbox publisher | Order Service, Notification Service |
| `eventcart.payments.completed` | Payment Service outbox publisher | Order Service, Notification Service |
| `eventcart.payments.failed` | Payment Service outbox publisher | Order Service, Inventory Service, Notification Service |
| `<topic>.dlq` | Error handlers | Developers/Admin diagnostics |

## MongoDB Collections

| Service | Collections |
| --- | --- |
| Catalog Service | `products`, `categories` |
| Cart Service | `carts` |
| Order Service | `orders`, `order_audit_logs`, `outbox_events` |
| Inventory Service | `inventory_items`, `inventory_reservations`, `outbox_events` |
| Payment Service | `payment_attempts`, `outbox_events` |
| Notification Service | `notifications` |

## How The Finished Application Will Be Used

Local usage will look like this:

```powershell
docker compose up -d
.\mvnw clean verify
.\mvnw spring-boot:run -pl services/catalog-service
```

Once all services are wired:

```powershell
docker compose up -d
.\mvnw clean package
```

Then APIs will be available through:

```text
http://localhost:8080
```

Swagger/OpenAPI documentation will be available per service, for example:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8083/swagger-ui.html
```

## Delivery Milestones

| Milestone | Result |
| --- | --- |
| 1 | Generate Maven multi-module Spring Boot project |
| 2 | Add Docker Compose for MongoDB, Kafka, Redis, Keycloak |
| 3 | Build Catalog Service with MongoDB CRUD APIs |
| 4 | Build Cart Service |
| 5 | Build Order Service and order MongoDB model |
| 6 | Add Kafka order-created event |
| 7 | Add Inventory Service and stock reservation |
| 8 | Add Payment Service and payment event flow |
| 9 | Add Notification Service |
| 10 | Add security with Keycloak and JWT |
| 11 | Add Kafka retry and DLQ handling |
| 12 | Add transactional outbox to order, inventory, and payment services |
| 13 | Add integration tests with Testcontainers |
| 14 | Add observability with Actuator, Prometheus, Grafana, and OpenTelemetry |
| 15 | Add Dockerfiles, CI/CD pipeline, Kubernetes manifests, and secret examples |
| 16 | Tighten customer ownership checks |
| 17 | Prepare interview notes and architecture explanation |
