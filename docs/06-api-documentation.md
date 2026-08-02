# API Documentation

EventCart uses springdoc-openapi to generate OpenAPI specifications and Swagger UI pages from Spring MVC controllers.

## Why OpenAPI Matters

OpenAPI gives us:

- Interactive API testing through Swagger UI.
- Machine-readable API contracts at `/v3/api-docs`.
- Request payload examples for common API use cases.
- Better communication between frontend, backend, QA, and platform teams.
- Interview-ready proof that the application has documented APIs.

## Current API Documentation URLs

| Service | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| Catalog Service | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| Cart Service | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| Order Service | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| Inventory Service | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| Payment Service | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |

## Catalog APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/products` | Create product |
| `GET` | `/api/v1/products/{productId}` | Get product by ID |
| `GET` | `/api/v1/products` | Search products |
| `PUT` | `/api/v1/products/{productId}` | Update product |
| `DELETE` | `/api/v1/products/{productId}` | Deactivate product |

## Cart APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/carts/{customerId}` | Get active customer cart |
| `POST` | `/api/v1/carts/{customerId}/items` | Add item to cart by product ID and quantity |
| `PUT` | `/api/v1/carts/{customerId}/items/{productId}` | Update item quantity |
| `DELETE` | `/api/v1/carts/{customerId}/items/{productId}` | Remove item |
| `DELETE` | `/api/v1/carts/{customerId}` | Clear cart |

Add item request:

```json
{
  "productId": "6a6f2ff6c33ef72269887fec",
  "quantity": 2
}
```

cart-service uses that product ID to call catalog-service and stores the catalog-derived product snapshot in MongoDB.

## Order APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/orders` | Place order from cart |
| `GET` | `/api/v1/orders/{orderId}` | Get order by ID |
| `GET` | `/api/v1/orders/customer/{customerId}` | List orders for a customer |

Place order request:

```json
{
  "customerId": "customer-1",
  "idempotencyKey": "customer-1-order-20260802-001"
}
```

`idempotencyKey` is optional, but recommended. It lets a frontend or API client retry the same order request without accidentally creating a duplicate order.

After the order is created, order-service returns the order with status `CREATED`. inventory-service processes the Kafka event asynchronously, payment-service reacts after inventory is reserved, and order-service updates the order to `INVENTORY_RESERVED`, `INVENTORY_FAILED`, `PAYMENT_COMPLETED`, or `PAYMENT_FAILED`.

## Inventory APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `PUT` | `/api/v1/inventory/{productId}` | Create or update product stock |
| `GET` | `/api/v1/inventory/{productId}` | Get product stock |
| `GET` | `/api/v1/inventory/reservations/{orderId}` | Get reservation result for an order |

Seed inventory request:

```json
{
  "sku": "SKU-1001",
  "productName": "Mechanical Keyboard",
  "availableQuantity": 25
}
```

Reservation statuses currently include `RESERVED`, `FAILED`, and `RELEASED`. `RELEASED` means stock was originally reserved but later returned to available stock after payment failed.

## Payment APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/payments/orders/{orderId}` | Get payment attempt for an order |

Payment attempt lookup:

```bash
curl http://localhost:8085/api/v1/payments/orders/<order-id>
```

payment-service creates the payment attempt asynchronously after it consumes `InventoryReservedEvent`.

## Run Services Locally

Start infrastructure:

```powershell
docker compose up -d
```

Run catalog-service:

```powershell
.\mvnw.cmd -pl services/catalog-service spring-boot:run
```

Run cart-service in another terminal:

```powershell
.\mvnw.cmd -pl services/cart-service spring-boot:run
```

Run order-service in another terminal:

```powershell
.\mvnw.cmd -pl services/order-service spring-boot:run
```

Run inventory-service in another terminal:

```powershell
.\mvnw.cmd -pl services/inventory-service spring-boot:run
```

Run payment-service in another terminal:

```powershell
.\mvnw.cmd -pl services/payment-service spring-boot:run
```

## Interview Notes

You should be able to explain:

- OpenAPI is a contract format for REST APIs.
- Swagger UI is an interactive UI generated from OpenAPI.
- Annotations such as `@Operation` and `@Tag` improve generated docs.
- Annotations such as `@ExampleObject` and `@Schema(example = "...")` make APIs easier to try from Swagger UI.
- API documentation helps consumers understand request bodies, responses, status codes, and failure cases.
- OpenAPI does not replace tests; it documents the contract, while tests verify behavior.
- API documentation should show service boundaries clearly. In this flow, clients do not send product price to cart-service.
- OpenAPI examples should demonstrate operationally safer requests, such as order placement with an `idempotencyKey`.
- Some APIs are lookup-only because their data is created asynchronously from Kafka events, such as payment attempts.
