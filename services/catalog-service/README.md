# catalog-service

`catalog-service` owns the product catalog for EventCart.

## Responsibility

This service manages products that customers can browse and add to their cart. It stores product data in MongoDB and exposes REST APIs for product management and search.

## Current Functionality

| Feature | Description |
| --- | --- |
| Create product | Adds a product with SKU, name, category, price, quantity, and tags |
| Get product | Fetches a product by MongoDB ID |
| Search products | Searches by keyword, category, active flag, and price range |
| Update product | Updates product details |
| Deactivate product | Marks a product inactive without deleting historical data |
| API documentation | Provides OpenAPI JSON and Swagger UI through springdoc |
| MongoDB indexes | Adds indexes for SKU, name, category/active, and active status |

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/products` | Create product |
| `GET` | `/api/v1/products/{productId}` | Get product by ID |
| `GET` | `/api/v1/products` | Search products |
| `PUT` | `/api/v1/products/{productId}` | Update product |
| `DELETE` | `/api/v1/products/{productId}` | Deactivate product |

## Local URLs

| Tool | URL |
| --- | --- |
| Service | `http://localhost:8081` |
| Health | `http://localhost:8081/actuator/health` |
| OpenAPI JSON | `http://localhost:8081/v3/api-docs` |
| Swagger UI | `http://localhost:8081/swagger-ui.html` |

## Interview Angle

This service demonstrates Spring Boot REST APIs, DTO validation, MongoDB document modeling, indexes, optimistic locking, error handling, and OpenAPI documentation.

