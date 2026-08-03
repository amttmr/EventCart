# MongoDB Process Details

This document explains how MongoDB is used in EventCart, including database ownership, document lifecycle, snapshots, outbox records, verification, and debugging.

## Page Summary

| Area | Details |
| --- | --- |
| Technology | MongoDB with Spring Data MongoDB. |
| Purpose | Store service-owned documents and outbox events. |
| Design style | Database per service, aggregate-oriented documents. |
| Important patterns | Snapshots, indexes, optimistic locking, auditing, transactional outbox. |

## Database Ownership Map

```mermaid
flowchart TD
    Mongo["MongoDB"]
    CatalogDb["eventcart_catalog"]
    CartDb["eventcart_cart"]
    OrderDb["eventcart_order"]
    InventoryDb["eventcart_inventory"]
    PaymentDb["eventcart_payment"]
    NotificationDb["eventcart_notification"]

    Products["products"]
    Carts["carts"]
    Orders["orders"]
    OrderOutbox["outbox_events"]
    Items["inventory_items"]
    Reservations["inventory_reservations"]
    InventoryOutbox["outbox_events"]
    Attempts["payment_attempts"]
    PaymentOutbox["outbox_events"]
    Notifications["notifications"]

    Mongo --> CatalogDb --> Products
    Mongo --> CartDb --> Carts
    Mongo --> OrderDb --> Orders
    OrderDb --> OrderOutbox
    Mongo --> InventoryDb --> Items
    InventoryDb --> Reservations
    InventoryDb --> InventoryOutbox
    Mongo --> PaymentDb --> Attempts
    PaymentDb --> PaymentOutbox
    Mongo --> NotificationDb --> Notifications
```

## Document Lifecycle In The Happy Path

```mermaid
sequenceDiagram
    participant Catalog as catalog-service
    participant Cart as cart-service
    participant Order as order-service
    participant Inventory as inventory-service
    participant Payment as payment-service
    participant Notification as notification-service
    participant Mongo as MongoDB

    Catalog->>Mongo: Insert products
    Inventory->>Mongo: Insert or update inventory_items
    Cart->>Mongo: Upsert carts with embedded product snapshot
    Order->>Mongo: Insert orders with cart item snapshot
    Order->>Mongo: Insert order outbox_events
    Inventory->>Mongo: Update inventory_items reserved quantity
    Inventory->>Mongo: Insert inventory_reservations
    Inventory->>Mongo: Insert inventory outbox_events
    Payment->>Mongo: Insert payment_attempts
    Payment->>Mongo: Insert payment outbox_events
    Notification->>Mongo: Insert notifications
```

## Snapshot Model

Cart and order documents intentionally store product snapshots.

```mermaid
flowchart LR
    Product["Product document<br/>name, sku, price"]
    CartItem["Cart item snapshot<br/>productId, sku, name, unitPrice"]
    OrderItem["Order item snapshot<br/>productId, sku, name, unitPrice, quantity"]

    Product -->|copied when item added| CartItem
    CartItem -->|copied when order placed| OrderItem
```

Why this matters:

- Cart can show product details without constantly joining to catalog.
- Order history remains stable even if product name or price changes later.
- Services can own their documents independently.

## Outbox Document Flow

```mermaid
stateDiagram-v2
    [*] --> PENDING: Business event saved
    PENDING --> PUBLISHED: Kafka send succeeds
    PENDING --> PENDING: Kafka send fails and attempts remain
    PENDING --> FAILED: Attempts exhausted
    FAILED --> PENDING: Manual retry or future repair process
    PUBLISHED --> [*]
```

Outbox fields usually answer these questions:

| Field | Question It Answers |
| --- | --- |
| `aggregateType` | Which business aggregate produced the event? |
| `aggregateId` | Which order, reservation, or payment does this event belong to? |
| `eventType` | What kind of event is this? |
| `topic` | Which Kafka topic should receive it? |
| `eventKey` | Which Kafka key is used? |
| `payloadJson` | What payload will be published? |
| `status` | Is it pending, published, or failed? |
| `publishAttempts` | How many publish attempts happened? |

## MongoDB Writes By Service

| Service | Main Writes | Trigger |
| --- | --- | --- |
| catalog-service | `products` | Admin product APIs. |
| cart-service | `carts` | Customer cart APIs. |
| order-service | `orders`, `outbox_events` | Place order API and consumed Kafka events. |
| inventory-service | `inventory_items`, `inventory_reservations`, `outbox_events` | Admin stock APIs and consumed Kafka events. |
| payment-service | `payment_attempts`, `outbox_events` | Consumed `InventoryReserved` event. |
| notification-service | `notifications` | Consumed business events. |

## Verification Commands

Open Mongo shell:

```powershell
docker exec -it eventcart-mongodb mongosh -u eventcart -p eventcart --authenticationDatabase admin
```

Check products:

```javascript
use eventcart_catalog
db.products.find().pretty()
```

Check cart:

```javascript
use eventcart_cart
db.carts.find({ customerId: "customer-1" }).pretty()
```

Check order and outbox:

```javascript
use eventcart_order
db.orders.find({ customerId: "customer-1" }).pretty()
db.outbox_events.find().sort({ createdAt: -1 }).pretty()
```

Check inventory:

```javascript
use eventcart_inventory
db.inventory_items.find().pretty()
db.inventory_reservations.find().pretty()
db.outbox_events.find().pretty()
```

Check payment:

```javascript
use eventcart_payment
db.payment_attempts.find().pretty()
db.outbox_events.find().pretty()
```

Check notifications:

```javascript
use eventcart_notification
db.notifications.find({ customerId: "customer-1" }).pretty()
```

## Debug Decision Tree

```mermaid
flowchart TD
    Problem["API or event result is wrong"]
    HasDoc{"Expected document exists?"}
    CheckApi["Check controller/service logs"]
    CheckIndexes["Check query field and indexes"]
    CheckOutbox{"Outbox event exists?"}
    CheckKafka["Check Kafka topic"]
    CheckStatus{"Outbox status?"}
    Pending["PENDING means scheduler/publish issue"]
    Failed["FAILED means publish repeatedly failed"]
    Published["PUBLISHED means check consumer side"]

    Problem --> HasDoc
    HasDoc -->|No| CheckApi
    HasDoc -->|Yes| CheckOutbox
    CheckApi --> CheckIndexes
    CheckOutbox -->|No| CheckApi
    CheckOutbox -->|Yes| CheckStatus
    CheckStatus -->|PENDING| Pending
    CheckStatus -->|FAILED| Failed
    CheckStatus -->|PUBLISHED| Published
    Published --> CheckKafka
```

## Best Practices Used

- Use one database per service.
- Use embedded documents for aggregate data loaded together.
- Use snapshots for order and cart history.
- Use indexes on lookup fields such as SKU, customer ID, status, and active flags.
- Use optimistic locking where concurrent updates are possible.
- Use DTOs at API boundaries instead of exposing documents directly.
- Use outbox records for event publishing reliability.

## Interview Explanation

"MongoDB is used as a service-owned document store. Carts and orders are aggregate-shaped, so embedded items and snapshots fit well. Each service owns its database, which keeps boundaries clear. The trade-off is that cross-service joins are not used; coordination happens through APIs and Kafka events."
