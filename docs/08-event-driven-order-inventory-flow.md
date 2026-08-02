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
9. inventory-service publishes either `InventoryReservedEvent` or `InventoryReservationFailedEvent`.
10. order-service consumes the inventory result event.
11. order-service updates the order status and clears the cart only after successful inventory reservation.
12. payment-service consumes `InventoryReservedEvent`.
13. payment-service stores a payment attempt and publishes `PaymentCompletedEvent` or `PaymentFailedEvent`.
14. order-service consumes the payment result event and updates final payment status.
15. inventory-service consumes `PaymentFailedEvent` and releases previously reserved stock.
16. notification-service consumes order, inventory failure, and payment result events and stores customer notifications.

## Topics

| Topic | Producer | Consumer | Purpose |
| --- | --- | --- | --- |
| `eventcart.orders.created` | order-service outbox publisher | inventory-service, notification-service | Tells downstream services an order was placed |
| `eventcart.inventory.reserved` | inventory-service | order-service, payment-service | Tells the system stock was reserved |
| `eventcart.inventory.failed` | inventory-service | order-service, notification-service | Tells the system stock could not be reserved |
| `eventcart.payments.completed` | payment-service | order-service, notification-service | Tells the system payment completed |
| `eventcart.payments.failed` | payment-service | order-service, inventory-service, notification-service | Tells the system payment failed |
| `<topic>.dlq` | Kafka retry recoverer | QA/developers | Preserves messages that failed all retries |

## MongoDB Collections

| Service | Database | Collection | Purpose |
| --- | --- | --- | --- |
| order-service | `eventcart_order` | `orders` | Stores order snapshots |
| order-service | `eventcart_order` | `outbox_events` | Stores pending and published order events |
| inventory-service | `eventcart_inventory` | `inventory_items` | Stores product stock |
| inventory-service | `eventcart_inventory` | `inventory_reservations` | Stores reservation results |
| payment-service | `eventcart_payment` | `payment_attempts` | Stores mock payment attempts |
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
- Kafka consumers retry failed processing and then publish exhausted records to `<topic>.dlq`.
- Event metadata carries a correlation ID, and Kafka listener threads copy it into MDC for logs.
- inventory-service releases stock after payment failure as a saga-style compensating action.
- Kafka producer and consumer infrastructure is configured explicitly in the services so the project does not depend on hidden auto-configuration.
- Cart cleanup after inventory reservation is best-effort. If cart-service is unavailable, the order status remains reserved and the warning log shows the cleanup failure.

## Current Limitations

- The outbox foundation is implemented first for order-created events; payment and inventory publishers still publish directly.
- MongoDB local compose uses a simple single-node setup, so multi-document transaction behavior is not the focus of the local environment.
- Notification delivery is stored as in-app history; no real email/SMS provider is integrated yet.

## Interview Talking Points

- Kafka decouples order-service from inventory-service.
- Events are business facts, not commands. `OrderCreatedEvent` says an order exists.
- Consumers must be idempotent because Kafka can deliver messages more than once.
- Event payloads should contain useful snapshots so consumers do not need extra synchronous calls.
- The outbox pattern helps avoid the "database save succeeded but Kafka publish failed" problem and is now visible in order-service.
- Redis idempotency protects order placement from duplicate client retries.
- payment-service is an example of event chaining: one event-driven service produces the next business fact.
- Inventory release after payment failure is a compensating action in a distributed workflow.
