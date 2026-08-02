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
| Place order | `Placing order`, `Cart fetched for order`, `Order saved`, `Publishing OrderCreated event` |
| Inventory reservation | `Consumed OrderCreated event`, `Reserving inventory`, `Inventory reserved` |
| Failed inventory | `Reservation stock check failed`, `Inventory reservation failed`, `Publishing InventoryReservationFailed event` |

## Interview Talking Points

- Use logs to explain the business flow, not just technical stack traces.
- Log at service boundaries: REST calls, Kafka publish, Kafka consume, and database state changes.
- Avoid logging secrets, credentials, tokens, or full customer payment data.
- Keep noisy details at `DEBUG` so normal local runs stay readable.
