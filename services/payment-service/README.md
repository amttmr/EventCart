# payment-service

`payment-service` owns mock payment processing for EventCart.

## Responsibility

This service consumes `InventoryReservedEvent` from Kafka, simulates a payment attempt, stores the attempt in MongoDB, and stores either `PaymentCompletedEvent` or `PaymentFailedEvent` in the MongoDB outbox before Kafka publication.

## Current Functionality

| Feature | Description |
| --- | --- |
| Consume inventory success | Listens to `eventcart.inventory.reserved` |
| Payment simulation | Completes or fails payment based on a configurable amount threshold |
| Payment attempt snapshot | Stores payment attempt result in MongoDB |
| Idempotent consumer | Skips duplicate processing when an order already has a payment attempt |
| Transactional outbox | Stores payment result events in `outbox_events` before Kafka publication |
| Kafka producer | Publishes pending payment outbox events |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/payments/orders/{orderId}` | Get payment attempt by order ID |

## Simulation Rule

The local mock provider is configured in `application.yml`:

```yaml
eventcart:
  payment:
    simulation:
      provider-name: MockPay
      failure-amount-threshold: 50000.00
```

Payments below `50000.00` complete successfully. Payments at or above `50000.00` fail with a mock decline reason.

## Kafka Topics

| Topic | Direction | Purpose |
| --- | --- | --- |
| `eventcart.inventory.reserved` | Consumes | Starts payment processing after stock is reserved |
| `eventcart.payments.completed` | Produces through outbox | Tells order-service payment completed |
| `eventcart.payments.failed` | Produces through outbox | Tells order-service payment failed |

## MongoDB Collection

| Database | Collection | Purpose |
| --- | --- | --- |
| `eventcart_payment` | `payment_attempts` | Stores one payment attempt per order |
| `eventcart_payment` | `outbox_events` | Stores pending, published, and failed payment result events |

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8085` |
| Health | `http://localhost:8085/actuator/health` |
| OpenAPI JSON | `http://localhost:8085/v3/api-docs` |
| Swagger UI | `http://localhost:8085/swagger-ui.html` |

## Interview Angle

This service demonstrates event chaining, deterministic external-provider simulation, idempotent Kafka consumers, MongoDB persistence, transactional outbox publishing, and eventual consistency across inventory, payment, and order status updates.
