# Detailed Application Flow

This document explains the complete EventCart business process in a Confluence-style format. It is useful for QA, new joiners, debugging, and interview preparation.

## Page Summary

| Area | Details |
| --- | --- |
| Purpose | Explain the full customer order flow from product creation to notification. |
| Main entry point | API Gateway on `localhost:8080`. |
| Main services | catalog, cart, order, inventory, payment, notification. |
| Main infrastructure | MongoDB, Kafka, Redis, Keycloak. |
| Consistency model | Synchronous for cart/catalog/order request setup, asynchronous after order creation. |

## End-To-End Business Flow

```mermaid
flowchart TD
    Start["Start"]
    Login["User gets JWT token from Keycloak"]
    Product["Admin creates or updates product"]
    Stock["Admin seeds inventory stock"]
    CartAdd["Customer adds product to cart"]
    CartSnapshot["Cart stores product snapshot"]
    PlaceOrder["Customer places order"]
    RedisCheck["Order service checks Redis idempotency key"]
    OrderSnapshot["Order service stores order snapshot in MongoDB"]
    OutboxOrder["OrderCreated stored in order outbox"]
    KafkaOrder["OrderCreated published to Kafka"]
    InventoryReserve["Inventory service reserves stock"]
    InventoryResult{"Stock available?"}
    InventoryFailed["Inventory failed event"]
    PaymentStart["Payment service starts payment simulation"]
    PaymentResult{"Payment successful?"}
    PaymentCompleted["Payment completed event"]
    PaymentFailed["Payment failed event"]
    ReleaseStock["Inventory releases reserved stock"]
    FinalSuccess["Order status PAYMENT_COMPLETED"]
    FinalInventoryFailed["Order status INVENTORY_FAILED"]
    FinalPaymentFailed["Order status PAYMENT_FAILED"]
    Notify["Notification service stores notifications"]
    End["End"]

    Start --> Login
    Login --> Product
    Product --> Stock
    Stock --> CartAdd
    CartAdd --> CartSnapshot
    CartSnapshot --> PlaceOrder
    PlaceOrder --> RedisCheck
    RedisCheck --> OrderSnapshot
    OrderSnapshot --> OutboxOrder
    OutboxOrder --> KafkaOrder
    KafkaOrder --> InventoryReserve
    InventoryReserve --> InventoryResult
    InventoryResult -->|No| InventoryFailed
    InventoryFailed --> FinalInventoryFailed
    InventoryFailed --> Notify
    InventoryResult -->|Yes| PaymentStart
    PaymentStart --> PaymentResult
    PaymentResult -->|Yes| PaymentCompleted
    PaymentCompleted --> FinalSuccess
    PaymentCompleted --> Notify
    PaymentResult -->|No| PaymentFailed
    PaymentFailed --> FinalPaymentFailed
    PaymentFailed --> ReleaseStock
    PaymentFailed --> Notify
    FinalSuccess --> End
    FinalInventoryFailed --> End
    FinalPaymentFailed --> End
```

## Request And Event Timeline

```mermaid
sequenceDiagram
    actor Admin
    actor Customer
    participant Keycloak
    participant Gateway as API Gateway
    participant Catalog as catalog-service
    participant Cart as cart-service
    participant Order as order-service
    participant Redis
    participant Kafka
    participant Inventory as inventory-service
    participant Payment as payment-service
    participant Notification as notification-service
    participant MongoDB

    Admin->>Keycloak: Get ADMIN token
    Admin->>Gateway: Create product
    Gateway->>Catalog: Forward product request
    Catalog->>MongoDB: Save product

    Admin->>Gateway: Seed inventory
    Gateway->>Inventory: Forward stock request
    Inventory->>MongoDB: Save inventory item

    Customer->>Keycloak: Get CUSTOMER token
    Customer->>Gateway: Add item to cart
    Gateway->>Cart: Forward cart request
    Cart->>Catalog: Fetch product details
    Cart->>MongoDB: Save cart with product snapshot

    Customer->>Gateway: Place order with idempotencyKey
    Gateway->>Order: Forward order request
    Order->>Redis: Reserve idempotency key
    Order->>Cart: Fetch cart snapshot
    Order->>MongoDB: Save order and outbox event
    Order->>Redis: Store ORDER:<order-id>
    Order-->>Customer: Return created order

    Order->>Kafka: Publish OrderCreated from outbox
    Kafka->>Inventory: Deliver OrderCreated
    Kafka->>Notification: Deliver OrderCreated
    Inventory->>MongoDB: Save reservation and inventory outbox event
    Inventory->>Kafka: Publish inventory result
    Kafka->>Order: Deliver inventory result
    Kafka->>Payment: Deliver InventoryReserved if successful
    Order->>MongoDB: Update order inventory status
    Payment->>MongoDB: Save payment attempt and payment outbox event
    Payment->>Kafka: Publish payment result
    Kafka->>Order: Deliver payment result
    Kafka->>Inventory: Deliver PaymentFailed if failed
    Kafka->>Notification: Deliver payment result
    Order->>MongoDB: Update final order status
    Notification->>MongoDB: Store notification projection
```

## API Calling Sequence

Use this sequence when manually testing the happy path.

| Step | Actor | API | Expected Result |
| --- | --- | --- | --- |
| 1 | Admin | Get Keycloak admin token | JWT with `ADMIN` role. |
| 2 | Admin | Create product | Product stored in `eventcart_catalog.products`. |
| 3 | Admin | Seed inventory | Stock stored in `eventcart_inventory.inventory_items`. |
| 4 | Customer | Get Keycloak customer token | JWT with `CUSTOMER` role and `customer_id`. |
| 5 | Customer | Add item to cart | Cart stored in `eventcart_cart.carts`. |
| 6 | Customer | Place order | Order stored in `eventcart_order.orders`; idempotency key stored in Redis. |
| 7 | System | Outbox publishes `OrderCreated` | Message appears on `eventcart.orders.created`. |
| 8 | System | Inventory reserves stock | Reservation stored in `eventcart_inventory.inventory_reservations`. |
| 9 | System | Payment is simulated | Payment attempt stored in `eventcart_payment.payment_attempts`. |
| 10 | System | Notifications are created | Notification stored in `eventcart_notification.notifications`. |
| 11 | Customer | Fetch order by ID | Final status becomes `PAYMENT_COMPLETED`, `PAYMENT_FAILED`, or `INVENTORY_FAILED`. |

## Service Responsibility Flow

```mermaid
flowchart LR
    Catalog["catalog-service<br/>Owns products"]
    Cart["cart-service<br/>Owns active carts"]
    Order["order-service<br/>Owns orders and idempotency"]
    Inventory["inventory-service<br/>Owns stock and reservations"]
    Payment["payment-service<br/>Owns payment attempts"]
    Notification["notification-service<br/>Owns notification projection"]

    Catalog -->|Product data for cart snapshot| Cart
    Cart -->|Cart snapshot for checkout| Order
    Order -->|OrderCreated event| Inventory
    Order -->|OrderCreated event| Notification
    Inventory -->|InventoryReserved event| Order
    Inventory -->|InventoryReserved event| Payment
    Inventory -->|InventoryReservationFailed event| Order
    Inventory -->|InventoryReservationFailed event| Notification
    Payment -->|PaymentCompleted event| Order
    Payment -->|PaymentCompleted event| Notification
    Payment -->|PaymentFailed event| Order
    Payment -->|PaymentFailed event| Inventory
    Payment -->|PaymentFailed event| Notification
```

## Order Status State Machine

```mermaid
stateDiagram-v2
    [*] --> CREATED: Order saved
    CREATED --> INVENTORY_RESERVED: InventoryReserved event
    CREATED --> INVENTORY_FAILED: InventoryReservationFailed event
    INVENTORY_RESERVED --> PAYMENT_COMPLETED: PaymentCompleted event
    INVENTORY_RESERVED --> PAYMENT_FAILED: PaymentFailed event
    INVENTORY_FAILED --> [*]
    PAYMENT_COMPLETED --> [*]
    PAYMENT_FAILED --> [*]
```

## Failure Paths

| Failure | Where It Happens | Expected Behavior | Verification |
| --- | --- | --- | --- |
| Empty cart | order-service before event publishing | API returns `EMPTY_CART`; no order should be created. | Check response and `eventcart_order.orders`. |
| Duplicate order request | order-service Redis idempotency | Same idempotency key returns existing order after completion. | Check Redis key and order count. |
| Insufficient stock | inventory-service after `OrderCreated` | Order becomes `INVENTORY_FAILED`; no payment attempt is created. | Check order status, inventory reservation, notification. |
| Payment declined | payment-service after `InventoryReserved` | Order becomes `PAYMENT_FAILED`; inventory reservation is released. | Check payment attempt and inventory reservation status. |
| Kafka listener failure | Spring Kafka listener | Message retries and then moves to `.dlq`. | Consume matching DLQ topic. |

## Developer Debug Map

```mermaid
flowchart TD
    Symptom["Order not in expected status"]
    CheckOrder["Check eventcart_order.orders"]
    CheckOutbox["Check outbox_events"]
    CheckKafka["Check Kafka topic and DLQ"]
    CheckInventory["Check inventory reservation"]
    CheckPayment["Check payment attempt"]
    CheckNotification["Check notification projection"]
    CheckLogs["Search logs by correlationId or orderId"]

    Symptom --> CheckOrder
    CheckOrder --> CheckOutbox
    CheckOutbox --> CheckKafka
    CheckKafka --> CheckInventory
    CheckInventory --> CheckPayment
    CheckPayment --> CheckNotification
    CheckNotification --> CheckLogs
```

## Confluence Notes

If pasting into Confluence:

- Keep the section tables as-is.
- Paste Mermaid blocks into a Mermaid macro if available.
- If Mermaid is not available, paste the Mermaid source as code and attach exported diagram images later.
- Keep API sequence and debug map on the same page so QA can follow the process without jumping between pages.
