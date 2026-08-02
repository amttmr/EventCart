# cart-service

`cart-service` owns the customer shopping cart for EventCart.

## Responsibility

This service stores a customer's active cart in MongoDB. It lets customers add product snapshots to their cart, update quantities, remove items, and clear the cart.

## Current Functionality

| Feature | Description |
| --- | --- |
| Get cart | Returns the active cart for a customer, creating an empty cart if needed |
| Add item | Adds a product snapshot to the cart or increases quantity if it already exists |
| Update quantity | Changes the quantity of an existing cart item |
| Remove item | Removes one item from the cart |
| Clear cart | Removes all items from the cart |
| Totals | Calculates total quantity and subtotal |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/carts/{customerId}` | Get active customer cart |
| `POST` | `/api/v1/carts/{customerId}/items` | Add item to cart |
| `PUT` | `/api/v1/carts/{customerId}/items/{productId}` | Update item quantity |
| `DELETE` | `/api/v1/carts/{customerId}/items/{productId}` | Remove item |
| `DELETE` | `/api/v1/carts/{customerId}` | Clear cart |

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8082` |
| Health | `http://localhost:8082/actuator/health` |
| OpenAPI JSON | `http://localhost:8082/v3/api-docs` |
| Swagger UI | `http://localhost:8082/swagger-ui.html` |

## Interview Angle

This service demonstrates service ownership, cart document modeling, embedded MongoDB documents, DTO validation, idempotent-ish add behavior, and separation between product catalog ownership and cart snapshots.

