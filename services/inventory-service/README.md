# inventory-service

`inventory-service` owns stock reservation for EventCart orders.

## Responsibility

This service consumes `OrderCreatedEvent` from Kafka, checks local inventory stock in MongoDB, reserves stock when possible, and stores either `InventoryReservedEvent` or `InventoryReservationFailedEvent` in the MongoDB outbox before Kafka publication. Successful reservation events include the order amount and currency so payment-service can simulate payment without calling order-service. It also consumes `PaymentFailedEvent` to release stock when payment fails.

## Current Functionality

| Feature | Description |
| --- | --- |
| Seed inventory | Provides an admin-style API to create or update local stock for a product |
| Consume order event | Listens to `eventcart.orders.created` |
| Reserve stock | Decreases available quantity and increases reserved quantity |
| Release stock | Releases reserved quantity back to available stock after payment failure |
| Reservation result | Stores reservation outcome in MongoDB |
| Transactional outbox | Stores inventory result events in `outbox_events` before Kafka publication |
| Kafka producer | Publishes pending inventory outbox events |
| Kafka compensation consumer | Consumes `PaymentFailedEvent` from `eventcart.payments.failed` |
| Payment handoff | Includes amount and currency on `InventoryReservedEvent` for payment-service |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `PUT` | `/api/v1/inventory/{productId}` | Create or update inventory stock |
| `GET` | `/api/v1/inventory/{productId}` | Get inventory item |
| `GET` | `/api/v1/inventory/reservations/{orderId}` | Get reservation result by order ID |

Reservation statuses:

| Status | Meaning |
| --- | --- |
| `RESERVED` | Stock was reserved for the order |
| `FAILED` | Stock could not be reserved |
| `RELEASED` | Stock was reserved, payment failed, and the stock was released |

## Seed Inventory Request

```json
{
  "sku": "SKU-1001",
  "productName": "Mechanical Keyboard",
  "availableQuantity": 25
}
```

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8084` |
| Health | `http://localhost:8084/actuator/health` |
| OpenAPI JSON | `http://localhost:8084/v3/api-docs` |
| Swagger UI | `http://localhost:8084/swagger-ui.html` |

## MongoDB Collections

| Database | Collection | Purpose |
| --- | --- | --- |
| `eventcart_inventory` | `inventory_items` | Stores product stock |
| `eventcart_inventory` | `inventory_reservations` | Stores reservation results |
| `eventcart_inventory` | `outbox_events` | Stores pending, published, and failed inventory result events |

## Interview Angle

This service demonstrates Kafka consumers, asynchronous event handling, idempotency checks, inventory reservation, compensating stock release, eventual consistency, retries, dead-letter topics, and the transactional outbox pattern for reliable event publishing.
