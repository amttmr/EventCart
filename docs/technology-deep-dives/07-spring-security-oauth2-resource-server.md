# Spring Security OAuth2 Resource Server

Spring Security OAuth2 Resource Server is the security layer used by EventCart services to validate JWT access tokens and enforce authorization rules.

## Where It Is Used

Shared servlet security:

```text
common/common-security/src/main/java/com/eventcart/common/security
```

API Gateway reactive security:

```text
services/api-gateway/src/main/java/com/eventcart/gateway/security
```

Configuration in service `application.yml` files:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8088/realms/eventcart
          jwk-set-uri: http://localhost:8088/realms/eventcart/protocol/openid-connect/certs
```

## Why It Is Used

EventCart uses Resource Server because the services do not log users in directly. Instead:

1. Keycloak authenticates the user.
2. Keycloak issues a JWT access token.
3. Client sends `Authorization: Bearer <token>`.
4. Gateway and backend services validate the token.
5. Spring Security creates an authenticated principal.
6. Controllers and policies enforce roles and ownership.

## Security Layers In EventCart

| Layer | Responsibility |
| --- | --- |
| API Gateway | First entry point, routes requests, validates JWT, applies route-level role rules. |
| Backend services | Validate JWT for direct calls and enforce method/controller rules. |
| `KeycloakJwtRoleConverter` | Converts Keycloak `realm_access.roles` into Spring `ROLE_*` authorities. |
| `CustomerAccessPolicy` | Enforces customer ownership using `customer_id` claim. |
| Internal service token | Allows narrow asynchronous service-to-service operation such as cart cleanup. |

## Authorization Model

EventCart combines:

- Role-based access control: `ADMIN`, `CUSTOMER`, `SUPPORT`.
- Object-level access control: customer token can access only its own customer ID.
- Internal service authorization: trusted backend operation protected by `X-EventCart-Internal-Token`.

This is stronger than role-only security.

## Best Practices

- Validate JWT issuer and signature.
- Do not trust unsigned tokens or decoded token content without validation.
- Use HTTPS outside local development.
- Keep role mapping explicit.
- Use method-level or controller-level authorization for sensitive APIs.
- Enforce ownership checks for customer-scoped resources.
- Avoid putting sensitive data in JWT claims.
- Keep access tokens short-lived.
- Do not disable security outside tests.
- Keep internal service tokens scoped and secret.
- Deny by default, permit only explicit paths.

## How To Verify Behavior

Start infrastructure and services:

```powershell
docker compose up -d keycloak
.\mvnw.cmd -pl services/api-gateway spring-boot:run
```

Call without token:

```bash
curl http://localhost:8080/api/v1/products
```

Expected:

```text
401 Unauthorized
```

Call with token:

```bash
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <customer-token>"
```

Verify ownership:

```bash
curl http://localhost:8080/api/v1/carts/customer-2 \
  -H "Authorization: Bearer <customer-token-for-customer-1>"
```

Expected:

```text
403 Forbidden
```

Disable security only for tests:

```yaml
eventcart:
  security:
    enabled: false
```

The E2E tests use this setting because they are focused on service behavior, Kafka, MongoDB, and Redis.

## How To Debug

Enable security logs temporarily:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

Debug checklist:

| Symptom | Check |
| --- | --- |
| 401 | Missing token, expired token, invalid issuer, bad signature, Keycloak unreachable. |
| 403 | Valid token but missing role or ownership claim. |
| Roles not recognized | Check `KeycloakJwtRoleConverter` and token `realm_access.roles`. |
| Customer ownership fails | Check `customer_id` claim. |
| Swagger blocked | Check security filter chain permit rules. |
| Gateway passes but backend rejects | Backend validates token too; check backend config. |

Useful endpoints:

```text
http://localhost:8088/realms/eventcart/.well-known/openid-configuration
http://localhost:8088/realms/eventcart/protocol/openid-connect/certs
```

## Real-Time Monitoring

Local:

- Watch service logs for 401 and 403 failures.
- Inspect JWT claims.
- Use Swagger with bearer token.

Production:

- Track 401/403 rates.
- Alert on sudden authentication failure spikes.
- Monitor Keycloak availability and JWKS fetch failures.
- Audit admin and role changes.
- Track gateway denied requests by route.

## Interview Preparation

You should be able to explain:

- Resource server vs authorization server.
- JWT validation flow.
- Role mapping from Keycloak to Spring Security.
- Difference between authentication, authorization, RBAC, and ownership.
- Why backend services validate tokens even behind a gateway.
- How JWKS key rotation works at a high level.
- Difference between servlet security and reactive gateway security.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is a resource server? | An API server that validates access tokens and protects resources. |
| Does Spring Security call Keycloak for every request? | It usually validates JWT locally using cached signing keys from JWKS. |
| Why map roles? | Keycloak stores realm roles in `realm_access.roles`, while Spring expects authorities such as `ROLE_ADMIN`. |
| What is the difference between 401 and 403? | 401 means authentication failed or is missing. 403 means authenticated but not allowed. |
| Why use customer ownership checks? | A `CUSTOMER` role alone is too broad. The token must match the requested customer resource. |

## EventCart Takeaway

Spring Security OAuth2 Resource Server teaches how real APIs validate JWTs, map identity-provider roles, enforce role-based rules, and protect customer-owned resources.

