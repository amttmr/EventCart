# api-gateway

API Gateway is the single HTTP entry point for EventCart.

## What This Module Does

- Routes public API calls to the correct backend service.
- Validates JWT tokens from Keycloak.
- Enforces role-based access at the edge.
- Propagates `X-Correlation-Id` to downstream services.
- Exposes actuator health, metrics, and Prometheus endpoints.

## Local Port

```text
http://localhost:8080
```

## Routes

| Gateway path | Target service |
| --- | --- |
| `/api/v1/products/**` | catalog-service |
| `/api/v1/carts/**` | cart-service |
| `/api/v1/orders/**` | order-service |
| `/api/v1/inventory/**` | inventory-service |
| `/api/v1/payments/**` | payment-service |
| `/api/v1/notifications/**` | notification-service |

## Roles

- `ADMIN`: product and inventory management, payment inspection, all customer flows.
- `CUSTOMER`: cart, order, and notification APIs.
- `SUPPORT`: order, payment, and notification lookup.

Backend services still enforce direct-port JWT validation and customer ownership checks. The gateway is the first security layer, not the only one.

## Interview Angle

This module demonstrates API Gateway routing, centralized authorization, cross-cutting observability, and why backend services should still validate tokens and ownership even when a gateway exists.
