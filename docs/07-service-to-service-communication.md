# Service-to-Service Communication

This document explains the first synchronous microservice interactions in EventCart: cart-service calling catalog-service, and order-service calling cart-service.

## Flow

When a customer adds an item to the cart, the client sends only product ID and quantity.

```json
{
  "productId": "6a6f2ff6c33ef72269887fec",
  "quantity": 2
}
```

cart-service then calls catalog-service:

```text
GET http://localhost:8081/api/v1/products/{productId}
```

If catalog-service returns an active product, cart-service stores a snapshot of product ID, SKU, product name, unit price, currency, and quantity inside the cart document.

## Why Store A Snapshot?

The cart should not trust product name or price sent by a frontend client. A user could modify request payloads. By reading product details from catalog-service, cart-service uses product data owned by the backend.

The cart stores a snapshot because cart items need stable display and pricing data for the customer's current shopping session. order-service stores another snapshot when the order is placed.

## Configuration

cart-service configures the catalog client in `application.yml`:

```yaml
eventcart:
  clients:
    catalog:
      base-url: http://localhost:8081
      connect-timeout: 2s
      read-timeout: 3s
```

The Java implementation uses Spring `RestClient` with a request factory that applies connection and read timeouts.

## Error Handling

| Situation | cart-service result |
| --- | --- |
| Product does not exist in catalog-service | `404 PRODUCT_NOT_AVAILABLE` |
| Product is inactive | `404 PRODUCT_NOT_AVAILABLE` |
| catalog-service returns a server error | `503 CATALOG_SERVICE_UNAVAILABLE` |
| catalog-service is down or times out | `503 CATALOG_SERVICE_UNAVAILABLE` |

This is intentionally simple for the first microservices step. Later we can add retries, circuit breakers, fallback caching, and metrics.

## Synchronous Communication Trade-Offs

Synchronous HTTP is easy to understand, easy to debug, and gives cart-service immediate product data. It is a good first choice when the user is actively waiting for an API response.

The trade-off is runtime coupling. If catalog-service is slow or unavailable, add-to-cart can fail. The caller also pays extra latency because cart-service waits for catalog-service before it can save the cart.

In interviews, explain this as a deliberate design choice:

- Use synchronous HTTP when the caller needs an immediate answer.
- Use timeouts so one slow service does not block forever.
- Convert remote failures into clear API errors.
- Add resilience patterns when the system becomes more production-like.

## What Comes Next

order-service now uses the same synchronous pattern to fetch a customer's cart before creating an order. After the order is saved, the flow becomes asynchronous: order-service publishes `OrderCreated`, and inventory-service consumes it from Kafka.
