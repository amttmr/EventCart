# order-service

`order-service` owns order placement and the first order lifecycle state for EventCart.

## Responsibility

This service creates an order from the customer's cart. It calls cart-service, stores an order snapshot in MongoDB, and publishes an `OrderCreated` Kafka event for downstream services.

## Current Functionality

| Feature | Description |
| --- | --- |
| Place order | Accepts a customer ID, fetches the customer's cart, and creates an order |
| Cart lookup | Uses Spring RestClient to call cart-service with timeout and error handling |
| Order snapshot | Stores cart item snapshots inside the order document |
| Kafka producer | Publishes `OrderCreatedEvent` to the `eventcart.orders.created` topic |
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
  "customerId": "customer-1"
}
```

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8083` |
| Health | `http://localhost:8083/actuator/health` |
| OpenAPI JSON | `http://localhost:8083/v3/api-docs` |
| Swagger UI | `http://localhost:8083/swagger-ui.html` |

## Interview Angle

This service demonstrates order ownership, synchronous HTTP calls to another service, MongoDB order snapshots, Kafka event publishing, and the consistency risk of saving to MongoDB and publishing to Kafka without an outbox pattern.
