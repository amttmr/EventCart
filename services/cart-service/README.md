# cart-service

`cart-service` owns the customer shopping cart for EventCart.

## Responsibility

This service stores a customer's active cart in MongoDB. It lets customers add products to their cart, update quantities, remove items, and clear the cart. When a product is added, the service calls catalog-service, fetches the latest product details, and stores a product snapshot inside the cart.

## Current Functionality

| Feature | Description |
| --- | --- |
| Get cart | Returns the active cart for a customer, creating an empty cart if needed |
| Add item | Accepts product ID and quantity, fetches product details from catalog-service, then stores a product snapshot |
| Update quantity | Changes the quantity of an existing cart item |
| Remove item | Removes one item from the cart |
| Clear cart | Removes all items from the cart |
| Totals | Calculates total quantity and subtotal |
| Catalog lookup | Uses Spring RestClient to call catalog-service with timeout and error handling |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/carts/{customerId}` | Get active customer cart |
| `POST` | `/api/v1/carts/{customerId}/items` | Add item to cart |
| `PUT` | `/api/v1/carts/{customerId}/items/{productId}` | Update item quantity |
| `DELETE` | `/api/v1/carts/{customerId}/items/{productId}` | Remove item |
| `DELETE` | `/api/v1/carts/{customerId}` | Clear cart |

## Add Item Request

The client sends only the product ID and quantity:

```json
{
  "productId": "6a6f2ff6c33ef72269887fec",
  "quantity": 2
}
```

cart-service fetches `sku`, name, price, currency, and active status from catalog-service before saving the cart item.

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8082` |
| Health | `http://localhost:8082/actuator/health` |
| OpenAPI JSON | `http://localhost:8082/v3/api-docs` |
| Swagger UI | `http://localhost:8082/swagger-ui.html` |

## Interview Angle

This service demonstrates service ownership, cart document modeling, embedded MongoDB documents, DTO validation, idempotent-ish add behavior, synchronous service-to-service communication, timeout/error handling, and separation between product catalog ownership and cart snapshots.
