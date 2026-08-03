# Event-Driven Order, Inventory, And Payment Flow

This document explains the Kafka-based business flow in EventCart.

## Flow

1. Customer places an order through order-service.
2. order-service calls cart-service and fetches the customer's cart.
3. order-service stores an order snapshot in MongoDB.
4. order-service stores `OrderCreatedEvent` in MongoDB collection `outbox_events`.
5. order-service outbox publisher sends pending events to Kafka topic `eventcart.orders.created`.
6. inventory-service consumes `OrderCreatedEvent`.
7. inventory-service checks its local stock in MongoDB.
8. inventory-service stores a reservation result.
9. inventory-service stores either `InventoryReservedEvent` or `InventoryReservationFailedEvent` in MongoDB collection `outbox_events`.
10. inventory-service outbox publisher sends pending inventory events to Kafka.
11. order-service consumes the inventory result event.
12. order-service updates the order status and clears the cart only after successful inventory reservation.
13. payment-service consumes `InventoryReservedEvent`.
14. payment-service stores a payment attempt.
15. payment-service stores `PaymentCompletedEvent` or `PaymentFailedEvent` in MongoDB collection `outbox_events`.
16. payment-service outbox publisher sends pending payment events to Kafka.
17. order-service consumes the payment result event and updates final payment status.
18. inventory-service consumes `PaymentFailedEvent` and releases previously reserved stock.
19. notification-service consumes order, inventory failure, and payment result events, stores customer notifications, and optionally sends email/SMS notifications.

## Topics

| Topic | Producer | Consumer | Purpose |
| --- | --- | --- | --- |
| `eventcart.orders.created` | order-service outbox publisher | inventory-service, notification-service | Tells downstream services an order was placed |
| `eventcart.inventory.reserved` | inventory-service outbox publisher | order-service, payment-service | Tells the system stock was reserved |
| `eventcart.inventory.failed` | inventory-service outbox publisher | order-service, notification-service | Tells the system stock could not be reserved |
| `eventcart.payments.completed` | payment-service outbox publisher | order-service, notification-service | Tells the system payment completed |
| `eventcart.payments.failed` | payment-service outbox publisher | order-service, inventory-service, notification-service | Tells the system payment failed |
| `<topic>.dlq` | Kafka retry recoverer | QA/developers | Preserves messages that failed all retries |

## MongoDB Collections

| Service | Database | Collection | Purpose |
| --- | --- | --- | --- |
| order-service | `eventcart_order` | `orders` | Stores order snapshots |
| order-service | `eventcart_order` | `outbox_events` | Stores pending and published order events |
| inventory-service | `eventcart_inventory` | `inventory_items` | Stores product stock |
| inventory-service | `eventcart_inventory` | `inventory_reservations` | Stores reservation results |
| inventory-service | `eventcart_inventory` | `outbox_events` | Stores pending and published inventory result events |
| payment-service | `eventcart_payment` | `payment_attempts` | Stores mock payment attempts |
| payment-service | `eventcart_payment` | `outbox_events` | Stores pending and published payment result events |
| notification-service | `eventcart_notification` | `notifications` | Stores customer notification history |

## Redis Keys

order-service uses Redis to store order idempotency keys:

```text
eventcart:orders:idempotency:<client-key>
```

The value starts as `IN_PROGRESS`. After the order is saved, it becomes `ORDER:<order-id>`. If the same completed key is retried, order-service returns the original order.

## Why Kafka Here?

Inventory reservation does not need to block the initial order API forever. Kafka lets order-service say "an order was created" and lets inventory-service react independently.

This introduces eventual consistency. Immediately after the order API returns, inventory may still be processing the event. The client or another service can check the order/reservation status later.

The order status is the user-facing view of that eventual consistency:

| Status | Meaning |
| --- | --- |
| `CREATED` | Order is saved and waiting for inventory result |
| `INVENTORY_RESERVED` | Stock was reserved and the customer's cart cleanup was triggered |
| `INVENTORY_FAILED` | Stock could not be reserved; `statusReason` explains why |
| `PAYMENT_COMPLETED` | Mock payment completed successfully |
| `PAYMENT_FAILED` | Mock payment failed; `statusReason` explains why |

The inventory reservation status also changes after compensation:

| Reservation status | Meaning |
| --- | --- |
| `RESERVED` | Stock is reserved for the order |
| `FAILED` | Stock could not be reserved |
| `RELEASED` | Stock was reserved, payment failed, and reserved quantity was returned to available stock |

## Payment Simulation

payment-service uses a deterministic mock provider rule:

```text
Amounts below 50000.00 complete. Amounts at or above 50000.00 fail.
```

The service consumes only successful inventory reservations, so payment does not run if stock could not be reserved.

If payment fails, inventory-service compensates by releasing the reserved quantities. This is the first saga-style compensation step in the project.

## Current Reliability Features

- order-service uses an outbox collection before publishing `OrderCreatedEvent`.
- inventory-service uses an outbox collection before publishing `InventoryReservedEvent` and `InventoryReservationFailedEvent`.
- payment-service uses an outbox collection before publishing `PaymentCompletedEvent` and `PaymentFailedEvent`.
- Kafka consumers retry failed processing and then publish exhausted records to `<topic>.dlq`.
- Event metadata carries a correlation ID, and Kafka listener threads copy it into MDC for logs.
- inventory-service releases stock after payment failure as a saga-style compensating action.
- Kafka producer and consumer infrastructure is configured explicitly in the services so the project does not depend on hidden auto-configuration.
- Cart cleanup after inventory reservation is best-effort. If cart-service is unavailable, the order status remains reserved and the warning log shows the cleanup failure.
- order-service forwards the caller JWT for cart reads and uses a narrowly scoped internal service token for asynchronous cart cleanup.
- notification-service persists notification history and can call configured email/SMS providers after saving a notification.

## Current Limitations

- MongoDB local compose uses a simple single-node setup, so multi-document transaction behavior is not the focus of the local environment.
- Email and SMS delivery are disabled by default until SMTP/Twilio credentials and customer contacts are configured.
- The Testcontainers end-to-end test is Docker-backed. It compiles everywhere, but it only runs the real Kafka/Mongo path when Docker is available.

## Interview Talking Points

- Kafka decouples order-service from inventory-service.
- Events are business facts, not commands. `OrderCreatedEvent` says an order exists.
- Consumers must be idempotent because Kafka can deliver messages more than once.
- Event payloads should contain useful snapshots so consumers do not need extra synchronous calls.
- The outbox pattern helps avoid the "database save succeeded but Kafka publish failed" problem and is now visible in order-service, inventory-service, and payment-service.
- Redis idempotency protects order placement from duplicate client retries.
- payment-service is an example of event chaining: one event-driven service produces the next business fact.
- Inventory release after payment failure is a compensating action in a distributed workflow.
