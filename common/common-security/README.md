# common-security

Shared Spring Security configuration for EventCart servlet-based services.

## What This Module Does

- Configures each backend service as a JWT resource server.
- Extracts Keycloak realm roles from JWT claims.
- Applies role-based authorization rules for catalog, cart, order, inventory, payment, and notification APIs.
- Enforces customer ownership through `CustomerAccessPolicy` for customer-scoped resources.
- Validates a narrow internal service token for asynchronous backend cleanup calls.
- Provides a property switch for tests and local troubleshooting.

## Main Concepts

- `ADMIN` can manage catalog and inventory and inspect operational payment data.
- `CUSTOMER` can use cart, order, and notification APIs.
- `SUPPORT` can inspect orders, payments, and notifications.
- Customer ownership checks compare the requested `customerId` with JWT claims such as `customer_id`, `customerId`, `preferred_username`, and `sub`.
- `X-EventCart-Internal-Token` is accepted only for the internal cart-clear request used after inventory reservation.
- Swagger and health endpoints remain publicly accessible for local learning and QA.

## Configuration

Set `eventcart.security.enabled=false` in a test profile to disable authentication for automated tests.

Set the internal service token from an environment variable or secret manager:

```yaml
eventcart:
  internal-service:
    header-name: X-EventCart-Internal-Token
    token: ${EVENTCART_INTERNAL_SERVICE_TOKEN}
```

Production-like local runs use Keycloak:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8088/realms/eventcart
          jwk-set-uri: http://localhost:8088/realms/eventcart/protocol/openid-connect/certs
```
