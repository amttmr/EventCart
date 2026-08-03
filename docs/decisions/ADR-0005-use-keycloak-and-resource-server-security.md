# ADR-0005: Use Keycloak And Spring Security OAuth2 Resource Server

## Status

Accepted.

## Context

EventCart needs realistic API security for learning and interview preparation. The application needs:

- User authentication.
- Role-based authorization.
- Customer ownership checks.
- JWT validation in backend services.
- A local identity provider that can run with Docker Compose.

## Decision

Use Keycloak as the identity provider and configure the gateway and backend services as Spring Security OAuth2 Resource Servers.

Key roles:

| Role | Purpose |
| --- | --- |
| `ADMIN` | Manage products, inventory, and operational data. |
| `CUSTOMER` | Access customer-owned cart, order, and notification resources. |
| `SUPPORT` | Inspect support-oriented data where allowed. |

Key ownership claim:

```text
customer_id
```

## Consequences

Positive:

- The project uses a real OAuth2/OIDC provider.
- JWTs can be obtained locally from Keycloak.
- Roles are mapped from Keycloak `realm_access.roles` to Spring `ROLE_*` authorities.
- Backend services remain protected even if traffic bypasses the gateway.
- Customer ownership is stricter than simple role checks.

Trade-offs:

- Local development requires Keycloak to be running unless security is disabled for tests.
- Role mapping must be maintained.
- Token issuer and JWK URLs must match the runtime environment.
- Developers need to understand both authentication and authorization.

## Alternatives Considered

| Alternative | Reason Not Chosen |
| --- | --- |
| Basic authentication | Too simple for modern microservice interviews. |
| Custom JWT issuer | Teaches less about real identity providers. |
| Gateway-only security | Risky because backend services would trust network position too much. |

## Interview Explanation

"Keycloak issues JWTs, and Spring Security Resource Server validates those tokens in the gateway and backend services. Realm roles are mapped to Spring authorities. For customer APIs, a role is not enough; the token's `customer_id` must match the customer resource being accessed."
