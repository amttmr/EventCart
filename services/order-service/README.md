# order-service

`order-service` owns order placement and order lifecycle updates for EventCart.

## Responsibility

This service creates an order from the customer's cart. It calls cart-service, stores an order snapshot in MongoDB, publishes an `OrderCreated` Kafka event, consumes inventory result events, updates order status, and clears the cart after successful inventory reservation.

## Current Functionality

| Feature | Description |
| --- | --- |
| Place order | Accepts a customer ID, fetches the customer's cart, and creates an order |
| Redis idempotency | Uses an optional `idempotencyKey` to make order-placement retries safe |
| Cart lookup | Uses Spring RestClient to call cart-service with timeout and error handling |
| Order snapshot | Stores cart item snapshots inside the order document |
| Kafka producer | Publishes `OrderCreatedEvent` to the `eventcart.orders.created` topic |
| Kafka consumers | Consumes `InventoryReservedEvent` and `InventoryReservationFailedEvent` |
| Order status updates | Moves orders from `CREATED` to `INVENTORY_RESERVED` or `INVENTORY_FAILED` |
| Cart cleanup | Clears the customer's cart after inventory has been reserved |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/orders` | Place order from cart |
| `GET` | `/api/v1/orders/{orderId}` | Get order by ID |
| `GET` | `/api/v1/orders/customer/{customerId}` | List orders for a customer |

## Place Order Request

```json
{
  "customerId": "customer-1",
  "idempotencyKey": "customer-1-order-20260802-001"
}
```

`idempotencyKey` is optional, but recommended for real clients. If the same completed key is submitted again, order-service returns the original order instead of creating a duplicate.

## Kafka Topics

| Topic | Direction | Purpose |
| --- | --- | --- |
| `eventcart.orders.created` | Produces | Notifies inventory-service that an order exists |
| `eventcart.inventory.reserved` | Consumes | Updates order status to `INVENTORY_RESERVED` |
| `eventcart.inventory.failed` | Consumes | Updates order status to `INVENTORY_FAILED` with a reason |

## Redis Usage

Order idempotency keys are stored in Redis using the prefix:

```text
eventcart:orders:idempotency:
```

The local TTL is configured as `30m` in `application.yml`.

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8083` |
| Health | `http://localhost:8083/actuator/health` |
| OpenAPI JSON | `http://localhost:8083/v3/api-docs` |
| Swagger UI | `http://localhost:8083/swagger-ui.html` |

## Interview Angle

This service demonstrates order ownership, synchronous HTTP calls to another service, MongoDB order snapshots, Redis idempotency, Kafka event publishing and consumption, eventual consistency, and the consistency risk of saving to MongoDB and publishing to Kafka without an outbox pattern.
