# Keycloak

Keycloak is the local identity provider used by EventCart to issue JWT access tokens for API authentication and role-based authorization.

## Where It Is Used

Keycloak runs locally through [compose.yaml](../../compose.yaml):

```text
container: eventcart-keycloak
local URL: http://localhost:8088
realm: eventcart
realm import: ops/keycloak/eventcart-realm.json
```

EventCart services validate tokens issued by:

```text
http://localhost:8088/realms/eventcart
```

The API Gateway and backend services use Keycloak JWTs through Spring Security OAuth2 Resource Server.

## Why Keycloak Is Used

Keycloak gives the project production-style identity without writing custom login code:

- User authentication.
- JWT access token issuance.
- Realm roles.
- Client configuration.
- Local admin console.
- OAuth2/OpenID Connect flows.
- Portable realm export for local setup.

This lets the project focus on resource-server security, role checks, and customer ownership.

## EventCart Roles

| Role | Intended Usage |
| --- | --- |
| `ADMIN` | Product and inventory management, broad access. |
| `CUSTOMER` | Cart, order, and notification operations for the matching customer. |
| `SUPPORT` | Support-style read access where allowed. |

The local `customer-user` token contains a `customer_id` claim used by ownership checks.

## How To Start And Verify

Start Keycloak:

```powershell
docker compose up -d keycloak
docker compose ps keycloak
```

Open admin console:

```text
http://localhost:8088
```

Default local admin:

```text
username: admin
password: admin
```

Fetch admin token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8088/realms/eventcart/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=eventcart-gateway&username=admin-user&password=admin"

$adminToken = $tokenResponse.access_token
```

Fetch customer token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8088/realms/eventcart/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=eventcart-gateway&username=customer-user&password=customer"

$customerToken = $tokenResponse.access_token
```

Use token:

```bash
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <token>"
```

## How To Inspect A JWT

A JWT has three parts:

```text
header.payload.signature
```

Useful claims:

| Claim | Meaning |
| --- | --- |
| `iss` | Issuer, must match Keycloak realm URL. |
| `sub` | Subject, usually user ID. |
| `exp` | Expiration time. |
| `realm_access.roles` | Realm roles such as `ADMIN` and `CUSTOMER`. |
| `customer_id` | EventCart customer ownership claim. |

You can inspect tokens with:

- Keycloak UI.
- jwt.io for local learning only.
- A local decoder script.

Do not paste production tokens into public tools.

## Best Practices

- Use Keycloak or another identity provider instead of custom password handling.
- Keep realm exports versioned for local/dev environments.
- Do not commit real production users, passwords, or client secrets.
- Keep access tokens short-lived.
- Use roles for coarse authorization and ownership checks for object-level authorization.
- Validate issuer and signature in every resource server.
- Use HTTPS in real environments.
- Separate local, staging, and production realms.
- Rotate secrets and credentials.
- Keep admin console access restricted.

## How To Debug

| Symptom | Check |
| --- | --- |
| `401 Unauthorized` | Token missing, expired, malformed, wrong issuer, or Keycloak unavailable. |
| `403 Forbidden` | Token is valid but missing role or customer ownership claim. |
| Backend cannot start | Check `issuer-uri` and `jwk-set-uri`. |
| Customer can access wrong data | Check `customer_id` claim and `CustomerAccessPolicy`. |
| Gateway works but direct service call fails | Backend services also validate JWTs and ownership. |

Useful URLs:

```text
http://localhost:8088/realms/eventcart/.well-known/openid-configuration
http://localhost:8088/realms/eventcart/protocol/openid-connect/certs
```

Logs:

```powershell
docker logs eventcart-keycloak
```

## Real-Time Monitoring

For local development:

- Check Keycloak container health.
- Use browser admin console.
- Verify tokens with curl.
- Watch service logs for authentication and authorization failures.

For production:

- Monitor login failures.
- Monitor token issuance rate.
- Monitor Keycloak latency and availability.
- Alert on realm/client configuration drift.
- Audit admin actions.

## Interview Preparation

You should be able to explain:

- Difference between authentication and authorization.
- What a realm is.
- What a client is.
- What JWT claims are.
- Difference between role-based access and ownership checks.
- Why services validate tokens instead of trusting the gateway only.
- What JWKS is.
- Why token expiration matters.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is Keycloak? | An identity provider that supports OAuth2/OpenID Connect and issues tokens. |
| What is a realm? | A tenant/security domain containing users, roles, clients, and settings. |
| What is JWKS? | A JSON Web Key Set. Resource servers use it to verify token signatures. |
| Why use roles and ownership? | Roles say what type of action is allowed; ownership says whether the caller can access this specific customer resource. |
| Should backend services trust only the gateway? | No. Defense in depth means backend services also validate tokens for direct-port or network bypass protection. |

## EventCart Takeaway

Keycloak gives EventCart a realistic security foundation. It teaches OAuth2, OpenID Connect, JWT claims, role mapping, customer ownership, and local identity-provider setup.

