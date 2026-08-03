# Service Responsibilities

This document explains the responsibility boundary for each EventCart service. The most important rule is that each service owns its data and exposes behavior through APIs or events.

## API Gateway

| Area | Details |
| --- | --- |
| Module | `services/api-gateway` |
| Port | `8080` |
| Main responsibility | Single entry point for client traffic. |
| Security | Validates JWTs from Keycloak and applies route-level role checks. |
| Data ownership | Owns no database. |

The gateway routes `/api/v1/**` requests to the correct backend service. It is useful for clients because they do not need to know every service port.

## Catalog Service

| Area | Details |
| --- | --- |
| Module | `services/catalog-service` |
| Port | `8081` |
| Database | `eventcart_catalog` |
| Collections | `products` |
| Main responsibility | Product creation, lookup, update, deactivation, and search. |

Catalog is the product source of truth. Cart service calls catalog-service to fetch product details before storing a cart item snapshot.

## Cart Service

| Area | Details |
| --- | --- |
| Module | `services/cart-service` |
| Port | `8082` |
| Database | `eventcart_cart` |
| Collections | `carts` |
| Main responsibility | Customer active cart. |

Cart stores product snapshots rather than only product IDs. This allows the cart to show product name, SKU, and price at the time the item was added.

Cart service uses synchronous HTTP to call catalog-service because the user request cannot complete until the product is validated.

## Order Service

| Area | Details |
| --- | --- |
| Module | `services/order-service` |
| Port | `8083` |
| Database | `eventcart_order` |
| Collections | `orders`, `outbox_events` |
| Redis usage | `eventcart:orders:idempotency:<key>` |
| Main responsibility | Order placement and order status lifecycle. |

Order service calls cart-service to fetch the customer's cart, stores an order snapshot, writes an `OrderCreated` outbox event, and later updates the order from inventory and payment events.

Order service is also where client retry safety is handled. The `idempotencyKey` prevents accidental duplicate order creation.

## Inventory Service

| Area | Details |
| --- | --- |
| Module | `services/inventory-service` |
| Port | `8084` |
| Database | `eventcart_inventory` |
| Collections | `inventory_items`, `inventory_reservations`, `outbox_events` |
| Main responsibility | Stock reservation and compensation. |

Inventory service consumes `OrderCreated` events. If enough stock exists, it reserves stock and publishes `InventoryReserved`. If not, it publishes `InventoryReservationFailed`.

When payment fails, inventory-service consumes `PaymentFailed` and releases reserved stock.

## Payment Service

| Area | Details |
| --- | --- |
| Module | `services/payment-service` |
| Port | `8085` |
| Database | `eventcart_payment` |
| Collections | `payment_attempts`, `outbox_events` |
| Main responsibility | Simulated payment processing. |

Payment service consumes `InventoryReserved`. It simulates either success or failure and publishes `PaymentCompleted` or `PaymentFailed` through its outbox.

This keeps the payment decision separate from order-service and gives us an interview-friendly example of asynchronous service choreography.

## Notification Service

| Area | Details |
| --- | --- |
| Module | `services/notification-service` |
| Port | `8086` |
| Database | `eventcart_notification` |
| Collections | `notifications` |
| Main responsibility | Notification projection and optional delivery. |

Notification service consumes business events and stores a customer-facing notification history. It can optionally deliver notifications through email or SMS providers.

## Common Modules

| Module | Responsibility |
| --- | --- |
| `common-events` | Kafka event contracts shared across services. |
| `common-web` | Shared JSON configuration, API error model, correlation ID support. |
| `common-security` | JWT resource server configuration, Keycloak role mapping, ownership checks. |
| `common-kafka` | Kafka retry and DLQ helper support. |
| `common-test` | Shared Testcontainers dependencies and test utilities. |

## Boundary Rules

- Services should not write into another service's MongoDB database.
- Services should communicate through APIs or Kafka events.
- Event payloads should contain enough context for consumers to avoid unnecessary synchronous calls.
- API DTOs and database documents should remain separate.
- Security checks should exist in backend services, not only in the gateway.
- Each service should be independently buildable as a Docker image.
