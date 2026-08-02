# inventory-service

`inventory-service` owns stock reservation for EventCart orders.

## Responsibility

This service consumes `OrderCreatedEvent` from Kafka, checks local inventory stock in MongoDB, reserves stock when possible, and publishes either `InventoryReservedEvent` or `InventoryReservationFailedEvent`. Successful reservation events include the order amount and currency so payment-service can simulate payment without calling order-service.

## Current Functionality

| Feature | Description |
| --- | --- |
| Seed inventory | Provides an admin-style API to create or update local stock for a product |
| Consume order event | Listens to `eventcart.orders.created` |
| Reserve stock | Decreases available quantity and increases reserved quantity |
| Reservation result | Stores reservation outcome in MongoDB |
| Kafka producer | Publishes `InventoryReservedEvent` or `InventoryReservationFailedEvent` |
| Payment handoff | Includes amount and currency on `InventoryReservedEvent` for payment-service |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `PUT` | `/api/v1/inventory/{productId}` | Create or update inventory stock |
| `GET` | `/api/v1/inventory/{productId}` | Get inventory item |
| `GET` | `/api/v1/inventory/reservations/{orderId}` | Get reservation result by order ID |

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

## Interview Angle

This service demonstrates Kafka consumers, asynchronous event handling, idempotency checks, inventory reservation, eventual consistency, and the need for stronger production patterns such as transactions, retries, dead-letter topics, and the outbox pattern.
