# Logging And Debugging

EventCart logs important business milestones so you can trace behavior across services while running locally.

## Default Log Level

Each service currently uses:

```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{correlationId:-}]"
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
| Place order | `Placing order`, `Order idempotency key reserved`, `Cart fetched for order`, `Order saved` |
| Outbox publishing | `OrderCreated event stored in outbox`, `Inventory event stored in outbox`, `Payment event stored in outbox`, `Outbox event published` |
| Inventory reservation | `Consumed OrderCreated event`, `Reserving inventory`, `Inventory reserved` |
| Failed inventory | `Reservation stock check failed`, `Inventory reservation failed`, `Inventory outbox event published` |
| Order status update | `Consumed InventoryReserved event`, `Order status updated after inventory reservation`, `Cart clear completed` |
| Order failure update | `Consumed InventoryReservationFailed event`, `Order status updated after inventory failure` |
| Payment processing | `Consumed InventoryReserved event`, `Processing payment`, `Payment completed`, `Payment failed`, `Payment outbox event published` |
| Final order payment update | `Consumed PaymentCompleted event`, `Consumed PaymentFailed event`, `Order status updated after payment` |
| Inventory compensation | `Consumed PaymentFailed event`, `Releasing inventory after payment failure`, `Inventory released after payment failure` |
| Notification projection | `Consumed OrderCreated event for notification`, `Notification stored`, `Email notification sent`, `SMS notification sent`, `Notification marked read` |
| Gateway routing/security | Gateway route logs, `401 Unauthorized`, `403 Forbidden`, and `X-Correlation-Id` response headers |

## Debug Correlation IDs

Every HTTP response includes `X-Correlation-Id`. If the request does not provide one, the gateway or backend service creates one.

Pass a known ID while debugging:

```bash
curl http://localhost:8080/api/v1/products \
  -H "X-Correlation-Id: qa-flow-001"
```

Expected behavior:

- The response contains `X-Correlation-Id: qa-flow-001`.
- Backend logs include the same ID in the log level pattern.
- Kafka event metadata carries the same correlation ID.

## Debug Kafka DLQs

If a listener fails after configured retries, the record is published to `<topic>.dlq`.

List DLQ topics:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list | Select-String ".dlq"
```

Read one DLQ:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic eventcart.orders.created.dlq --from-beginning --max-messages 5
```

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

## Debug Outbox Collections

Each event-producing service stores pending events before Kafka publication:

```javascript
use eventcart_order
db.outbox_events.find().sort({ createdAt: -1 }).limit(5).pretty()

use eventcart_inventory
db.outbox_events.find().sort({ createdAt: -1 }).limit(5).pretty()

use eventcart_payment
db.outbox_events.find().sort({ createdAt: -1 }).limit(5).pretty()
```

Expected successful publishing state is `PUBLISHED`. If Kafka is down, records stay `PENDING` until the scheduler retries. After repeated failures, records move to `FAILED` with `lastError`.

## Debug Ownership And Internal Calls

- Customer APIs require the JWT customer claim to match the requested `customerId`, unless the caller has `ADMIN` or `SUPPORT`.
- Keycloak local user `customer-user` includes `customer_id=customer-1`.
- order-service forwards the incoming bearer token when it reads the cart during order placement.
- order-service uses `X-EventCart-Internal-Token` only when a Kafka listener clears the cart after inventory reservation.
- In real environments, set `EVENTCART_INTERNAL_SERVICE_TOKEN` from a secret store and avoid logging the token value.

## Debug Notification Providers

notification-service always stores notification history first. Email and SMS provider calls happen after the notification record is saved.

If real delivery is missing:

- Check `eventcart.notifications.email.enabled` and `eventcart.notifications.sms.enabled`.
- Check customer contact configuration under `eventcart.notifications.contacts`.
- Check SMTP or Twilio environment variables.
- Check notification-service logs for provider delivery warnings.

## Interview Talking Points

- Use logs to explain the business flow, not just technical stack traces.
- Log at service boundaries: REST calls, Kafka publish, Kafka consume, and database state changes.
- Avoid logging secrets, credentials, tokens, or full customer payment data.
- Keep noisy details at `DEBUG` so normal local runs stay readable.
- Idempotency logs help explain safe retries and duplicate-request handling in interviews.
