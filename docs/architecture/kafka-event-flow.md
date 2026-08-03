# Kafka Event Flow

Kafka is used for the asynchronous part of the EventCart order workflow. The goal is to decouple order placement from inventory reservation, payment processing, and notification projection.

## Topics

| Topic | Producer | Consumers | Purpose |
| --- | --- | --- | --- |
| `eventcart.orders.created` | order-service | inventory-service, notification-service | Order was created from a cart. |
| `eventcart.inventory.reserved` | inventory-service | order-service, payment-service | Stock was reserved successfully. |
| `eventcart.inventory.failed` | inventory-service | order-service, notification-service | Stock reservation failed. |
| `eventcart.payments.completed` | payment-service | order-service, notification-service | Payment completed successfully. |
| `eventcart.payments.failed` | payment-service | order-service, inventory-service, notification-service | Payment failed and inventory should be released. |

Each topic also has a matching dead-letter topic with `.dlq` suffix, for example `eventcart.orders.created.dlq`.

## Happy Path

```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant Cart as cart-service
    participant Order as order-service
    participant Kafka
    participant Inventory as inventory-service
    participant Payment as payment-service
    participant Notification as notification-service

    Client->>Gateway: POST /api/v1/orders
    Gateway->>Order: Forward request with JWT
    Order->>Cart: Get customer cart
    Cart-->>Order: Cart snapshot
    Order->>Order: Save order and outbox event
    Order-->>Client: Order accepted
    Order->>Kafka: Publish OrderCreated from outbox
    Kafka->>Inventory: Consume OrderCreated
    Kafka->>Notification: Consume OrderCreated
    Inventory->>Inventory: Reserve stock and save outbox event
    Inventory->>Kafka: Publish InventoryReserved
    Kafka->>Order: Consume InventoryReserved
    Kafka->>Payment: Consume InventoryReserved
    Order->>Order: Update status to INVENTORY_RESERVED
    Payment->>Payment: Simulate payment and save outbox event
    Payment->>Kafka: Publish PaymentCompleted
    Kafka->>Order: Consume PaymentCompleted
    Kafka->>Notification: Consume PaymentCompleted
    Order->>Order: Update status to PAYMENT_COMPLETED
```

## Inventory Failure Path

```mermaid
sequenceDiagram
    participant Order as order-service
    participant Kafka
    participant Inventory as inventory-service
    participant Notification as notification-service

    Order->>Kafka: OrderCreated
    Kafka->>Inventory: Consume OrderCreated
    Inventory->>Inventory: Stock check fails
    Inventory->>Kafka: InventoryReservationFailed
    Kafka->>Order: Consume InventoryReservationFailed
    Kafka->>Notification: Consume InventoryReservationFailed
    Order->>Order: Update status to INVENTORY_FAILED
```

## Payment Failure And Compensation Path

```mermaid
sequenceDiagram
    participant Inventory as inventory-service
    participant Kafka
    participant Payment as payment-service
    participant Order as order-service
    participant Notification as notification-service

    Inventory->>Kafka: InventoryReserved
    Kafka->>Payment: Consume InventoryReserved
    Payment->>Payment: Simulated payment fails
    Payment->>Kafka: PaymentFailed
    Kafka->>Order: Consume PaymentFailed
    Kafka->>Inventory: Consume PaymentFailed
    Kafka->>Notification: Consume PaymentFailed
    Order->>Order: Update status to PAYMENT_FAILED
    Inventory->>Inventory: Release reserved stock
```

## Retry And DLQ Flow

```mermaid
flowchart TD
    Message["Kafka message received"]
    Listener["Spring Kafka listener"]
    Success["Processing succeeds<br/>offset can commit"]
    Retry["Retry with backoff"]
    DLQ["Publish original message to<br/>source-topic.dlq"]
    Investigate["Developer investigates DLQ record"]

    Message --> Listener
    Listener -->|no exception| Success
    Listener -->|exception| Retry
    Retry -->|retry succeeds| Success
    Retry -->|retries exhausted| DLQ
    DLQ --> Investigate
```

## Outbox Publishing Flow

```mermaid
flowchart LR
    Business["Business operation"]
    MongoState["Save business document"]
    Outbox["Save outbox_events document"]
    Scheduler["Outbox scheduler"]
    Kafka["Publish to Kafka"]
    Published["Mark outbox event PUBLISHED"]
    Failed["Increase attempts or mark FAILED"]

    Business --> MongoState
    MongoState --> Outbox
    Outbox --> Scheduler
    Scheduler --> Kafka
    Kafka --> Published
    Kafka --> Failed
```

## Debug Checklist

When an order does not reach the expected final status:

1. Check order-service logs for order placement and outbox messages.
2. Check `eventcart_order.orders` for order status.
3. Check `eventcart_order.outbox_events` for pending or failed events.
4. Check Kafka topic contents with `kafka-console-consumer`.
5. Check inventory-service logs and `eventcart_inventory.inventory_reservations`.
6. Check payment-service logs and `eventcart_payment.payment_attempts`.
7. Check notification-service logs and `eventcart_notification.notifications`.
8. Check `.dlq` topics when a listener throws repeatedly.

## Interview Talking Point

This is a choreography-based saga. No single orchestrator service tells every service what to do. Instead, each service reacts to events, updates its own state, and publishes the next event. This improves decoupling, but it requires idempotent consumers, good monitoring, retry/DLQ behavior, and clear event contracts.
