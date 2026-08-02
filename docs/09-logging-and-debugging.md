# Logging And Debugging

EventCart logs important business milestones so you can trace behavior across services while running locally.

## Default Log Level

Each service currently uses:

```yaml
logging:
  level:
    com.eventcart: INFO
```

`INFO` shows major business actions. `WARN` shows expected failure paths such as missing products, empty carts, insufficient stock, or remote service failures.

## Enable Debug Logs

Temporarily change one service's `application.yml` while debugging:

```yaml
logging:
  level:
    com.eventcart: DEBUG
```

`DEBUG` adds lower-level details such as service-to-service lookup results, search criteria, cart item count, and per-item inventory reservation changes.

## What To Watch

| Flow | Useful log lines |
| --- | --- |
| Create product | `Creating product`, `Product created` |
| Add to cart | `Adding cart item`, `Calling catalog-service`, `Cart item added` |
| Place order | `Placing order`, `Order idempotency key reserved`, `Cart fetched for order`, `Order saved`, `Publishing OrderCreated event` |
| Inventory reservation | `Consumed OrderCreated event`, `Reserving inventory`, `Inventory reserved` |
| Failed inventory | `Reservation stock check failed`, `Inventory reservation failed`, `Publishing InventoryReservationFailed event` |
| Order status update | `Consumed InventoryReserved event`, `Order status updated after inventory reservation`, `Cart clear completed` |
| Order failure update | `Consumed InventoryReservationFailed event`, `Order status updated after inventory failure` |
| Payment processing | `Consumed InventoryReserved event`, `Processing payment`, `Payment completed`, `Payment failed` |
| Final order payment update | `Consumed PaymentCompleted event`, `Consumed PaymentFailed event`, `Order status updated after payment` |

## Debug Redis Idempotency

When Redis is running through Docker Compose, connect to it:

```powershell
docker exec -it eventcart-redis redis-cli
```

List order idempotency keys:

```text
KEYS eventcart:orders:idempotency:*
```

Check a key value:

```text
GET eventcart:orders:idempotency:customer-1-order-20260802-001
```

During processing, the value is `IN_PROGRESS`. After the order is saved, the value becomes `ORDER:<order-id>`.

## Interview Talking Points

- Use logs to explain the business flow, not just technical stack traces.
- Log at service boundaries: REST calls, Kafka publish, Kafka consume, and database state changes.
- Avoid logging secrets, credentials, tokens, or full customer payment data.
- Keep noisy details at `DEBUG` so normal local runs stay readable.
- Idempotency logs help explain safe retries and duplicate-request handling in interviews.
