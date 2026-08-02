# common-security

Shared Spring Security configuration for EventCart servlet-based services.

## What This Module Does

- Configures each backend service as a JWT resource server.
- Extracts Keycloak realm roles from JWT claims.
- Applies role-based authorization rules for catalog, cart, order, inventory, payment, and notification APIs.
- Provides a property switch for tests and local troubleshooting.

## Main Concepts

- `ADMIN` can manage catalog and inventory and inspect operational payment data.
- `CUSTOMER` can use cart, order, and notification APIs.
- `SUPPORT` can inspect orders, payments, and notifications.
- Swagger and health endpoints remain publicly accessible for local learning and QA.

## Configuration

Set `eventcart.security.enabled=false` in a test profile to disable authentication for automated tests.

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
