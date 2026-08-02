# API Documentation

EventCart uses springdoc-openapi to generate OpenAPI specifications and Swagger UI pages from Spring MVC controllers.

## Why OpenAPI Matters

OpenAPI gives us:

- Interactive API testing through Swagger UI.
- Machine-readable API contracts at `/v3/api-docs`.
- Better communication between frontend, backend, QA, and platform teams.
- Interview-ready proof that the application has documented APIs.

## Current API Documentation URLs

| Service | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| Catalog Service | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| Cart Service | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |

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
| `POST` | `/api/v1/carts/{customerId}/items` | Add item to cart |
| `PUT` | `/api/v1/carts/{customerId}/items/{productId}` | Update item quantity |
| `DELETE` | `/api/v1/carts/{customerId}/items/{productId}` | Remove item |
| `DELETE` | `/api/v1/carts/{customerId}` | Clear cart |

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

## Interview Notes

You should be able to explain:

- OpenAPI is a contract format for REST APIs.
- Swagger UI is an interactive UI generated from OpenAPI.
- Annotations such as `@Operation` and `@Tag` improve generated docs.
- API documentation helps consumers understand request bodies, responses, status codes, and failure cases.
- OpenAPI does not replace tests; it documents the contract, while tests verify behavior.

