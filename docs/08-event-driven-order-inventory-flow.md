# Event-Driven Order And Inventory Flow

This document explains the first Kafka-based business flow in EventCart.

## Flow

1. Customer places an order through order-service.
2. order-service calls cart-service and fetches the customer's cart.
3. order-service stores an order snapshot in MongoDB.
4. order-service publishes `OrderCreatedEvent` to Kafka topic `eventcart.orders.created`.
5. inventory-service consumes `OrderCreatedEvent`.
6. inventory-service checks its local stock in MongoDB.
7. inventory-service stores a reservation result.
8. inventory-service publishes either `InventoryReservedEvent` or `InventoryReservationFailedEvent`.

## Topics

| Topic | Producer | Consumer | Purpose |
| --- | --- | --- | --- |
| `eventcart.orders.created` | order-service | inventory-service | Tells downstream services an order was placed |
| `eventcart.inventory.reserved` | inventory-service | Future order-service/payment-service | Tells the system stock was reserved |
| `eventcart.inventory.failed` | inventory-service | Future order-service/notification-service | Tells the system stock could not be reserved |

## MongoDB Collections

| Service | Database | Collection | Purpose |
| --- | --- | --- | --- |
| order-service | `eventcart_order` | `orders` | Stores order snapshots |
| inventory-service | `eventcart_inventory` | `inventory_items` | Stores product stock |
| inventory-service | `eventcart_inventory` | `inventory_reservations` | Stores reservation results |

## Why Kafka Here?

Inventory reservation does not need to block the initial order API forever. Kafka lets order-service say "an order was created" and lets inventory-service react independently.

This introduces eventual consistency. Immediately after the order API returns, inventory may still be processing the event. The client or another service can check the order/reservation status later.

## Current Limitations

This first implementation intentionally keeps the event flow simple:

- order-service saves to MongoDB and then publishes to Kafka directly.
- inventory-service updates stock and saves the reservation directly.
- There is no retry topic, dead-letter topic, distributed tracing, or outbox pattern yet.
- Kafka producer and consumer infrastructure is configured explicitly in the services so the project does not depend on hidden auto-configuration.

These are useful next interview topics because they show how a basic working event flow becomes production-ready.

## Interview Talking Points

- Kafka decouples order-service from inventory-service.
- Events are business facts, not commands. `OrderCreatedEvent` says an order exists.
- Consumers must be idempotent because Kafka can deliver messages more than once.
- Event payloads should contain useful snapshots so consumers do not need extra synchronous calls.
- The outbox pattern helps avoid the "database save succeeded but Kafka publish failed" problem.
