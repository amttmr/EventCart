# Redis Process Details

This document explains how Redis is used in EventCart for order idempotency.

## Page Summary

| Area | Details |
| --- | --- |
| Technology | Redis with Spring Data Redis. |
| Used by | order-service. |
| Purpose | Prevent duplicate order creation when clients retry checkout requests. |
| Data type | String key/value. |
| TTL | 30 minutes by default. |

## Why Redis Exists In The Flow

Order placement is a risky operation to duplicate. A duplicate request can happen if:

- The client retries after a timeout.
- The user double-clicks a checkout button.
- The gateway or network drops the response.
- A mobile app retries automatically.

Redis gives order-service a fast atomic way to reserve and reuse an idempotency key.

## Key Design

Key:

```text
eventcart:orders:idempotency:<idempotencyKey>
```

Possible values:

| Value | Meaning |
| --- | --- |
| `IN_PROGRESS` | The first request is currently creating an order. |
| `ORDER:<order-id>` | The request completed and created this order. |

Default TTL:

```text
30m
```

## Idempotency Flow

```mermaid
sequenceDiagram
    participant Client
    participant Order as order-service
    participant Redis
    participant Cart as cart-service
    participant Mongo as MongoDB

    Client->>Order: POST /orders with idempotencyKey
    Order->>Redis: GET idempotency key
    alt Key does not exist
        Order->>Redis: SET key = IN_PROGRESS with TTL if absent
        Order->>Cart: Fetch cart snapshot
        Order->>Mongo: Save order and outbox event
        Order->>Redis: SET key = ORDER:<order-id> with TTL
        Order-->>Client: Return new order
    else Key contains ORDER:<order-id>
        Order-->>Client: Return existing order
    else Key contains IN_PROGRESS
        Order-->>Client: Reject duplicate in-flight request
    end
```

## State Machine

```mermaid
stateDiagram-v2
    [*] --> Missing: No Redis key
    Missing --> InProgress: First request reserves key
    InProgress --> Completed: Order saved successfully
    InProgress --> Missing: Order creation fails and key is cleared
    Completed --> Completed: Duplicate retry returns existing order
    Completed --> Expired: TTL expires
    Expired --> [*]
```

## Duplicate Request Scenarios

| Scenario | Redis State | Expected Result |
| --- | --- | --- |
| First request | Missing | Reserve key and create order. |
| Same key while first request is still running | `IN_PROGRESS` | Return duplicate/in-progress error. |
| Same key after order completed | `ORDER:<order-id>` | Return existing order. |
| Same key after TTL expired | Missing | Treat as a new request. |
| Order creation fails before completion | `IN_PROGRESS` then cleared | Future retry can create order. |

## Redis And MongoDB Relationship

```mermaid
flowchart LR
    Client["Client retry behavior"]
    Redis["Redis<br/>temporary idempotency state"]
    OrderMongo["MongoDB<br/>orders source of truth"]
    Kafka["Kafka<br/>OrderCreated event"]

    Client --> Redis
    Redis -->|allows first request| OrderMongo
    OrderMongo --> Kafka
    Redis -->|returns existing order id| OrderMongo
```

Redis does not own the order. It only stores retry coordination state. MongoDB remains the source of truth for orders.

## Verification Commands

Open Redis CLI:

```powershell
docker exec -it eventcart-redis redis-cli
```

List idempotency keys:

```text
KEYS eventcart:orders:idempotency:*
```

Read one key:

```text
GET eventcart:orders:idempotency:customer-1-order-20260802-001
```

Check TTL:

```text
TTL eventcart:orders:idempotency:customer-1-order-20260802-001
```

Expected values:

```text
IN_PROGRESS
ORDER:<order-id>
```

## Debug Decision Tree

```mermaid
flowchart TD
    Issue["Duplicate order or retry issue"]
    KeyExists{"Redis key exists?"}
    Missing["Missing key<br/>request may be new or TTL expired"]
    InProgress["IN_PROGRESS<br/>first request still running or failed cleanup"]
    Completed["ORDER:<order-id><br/>expected duplicate retry behavior"]
    CheckMongo["Check MongoDB order document"]
    CheckLogs["Check order-service logs"]
    CheckTTL["Check TTL"]

    Issue --> KeyExists
    KeyExists -->|No| Missing
    KeyExists -->|Yes| CheckTTL
    CheckTTL --> InProgress
    CheckTTL --> Completed
    Missing --> CheckLogs
    InProgress --> CheckLogs
    Completed --> CheckMongo
```

## Developer Notes

- Always send an `idempotencyKey` from real checkout clients.
- Use a stable key for one checkout attempt, not a new key on every retry.
- Do not use Redis as the order source of truth.
- Keep TTL long enough for realistic client retries, but not permanent.
- Make failures clear `IN_PROGRESS` when order creation does not finish.

## Interview Explanation

"Redis is used to prevent duplicate orders during checkout retries. The first request reserves an idempotency key as `IN_PROGRESS`, then stores `ORDER:<order-id>` after success. A later retry with the same key returns the existing order. This is fast and atomic, but MongoDB is still the source of truth for orders."
