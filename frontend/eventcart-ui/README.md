# EventCart UI

EventCart UI is the React front end for the Real-Time E-Commerce Order Platform. It is a developer-friendly operations console for learning React while exercising the existing Spring Boot, Kafka, MongoDB, Redis, Keycloak, and API Gateway flow.

## What This Module Provides

The UI currently provides:

| Screen | Functionality |
| --- | --- |
| Dashboard | Shows active product count, cart count, order count, unread notifications, and the current end-to-end flow |
| Catalog | Searches catalog products and adds selected products to the active customer cart |
| Cart | Adds products by ID, changes item quantity, removes items, clears cart, and places orders with an idempotency key |
| Orders | Lists customer orders, polls async status changes, and inspects inventory/payment state for one order |
| Notifications | Lists customer notifications and marks notifications as read |
| Admin | Creates catalog products, seeds inventory stock, and inspects inventory state |

## Technologies Used

| Technology | Usage in this module |
| --- | --- |
| React | Component model and UI rendering |
| TypeScript | Compile-time safety for API responses, form data, and component props |
| Vite | Local dev server, fast refresh, production build |
| React Router | Page routing and protected route handling |
| TanStack React Query | Server-state fetching, caching, invalidation, polling, retries |
| Axios | HTTP client for API Gateway calls |
| Keycloak JS | Browser login and token management |
| Zustand | Small client-side workflow state such as active customer, selected product, and last order |
| React Hook Form | Efficient forms with low re-render overhead |
| Zod | Runtime form validation and type inference |
| Lucide React | Accessible icon buttons and navigation icons |
| Vitest | Unit tests for frontend utilities |
| Oxlint | Fast lint checks for React and TypeScript |

## Prerequisites

Install Node.js 22 or later. This machine currently has Node `22.x`, which is enough for the generated Vite app.

Backend prerequisites are the same as the root project:

- Docker Desktop running
- MongoDB, Kafka, Redis, Keycloak from `docker compose up -d`
- Backend services running
- API Gateway running on `http://localhost:8080`

## Install Dependencies

From the UI folder:

```powershell
cd C:\Users\HP\Documents\Study\EventCart\frontend\eventcart-ui
npm.cmd install
```

Use `npm.cmd` on Windows PowerShell if `npm` is blocked by the execution policy.

## Environment

Copy the example file when you need local overrides:

```powershell
Copy-Item .env.example .env.local
```

Default values:

```text
VITE_API_BASE_URL=/api/v1
VITE_AUTH_ENABLED=true
VITE_KEYCLOAK_URL=http://localhost:8088
VITE_KEYCLOAK_REALM=eventcart
VITE_KEYCLOAK_CLIENT_ID=eventcart-gateway
VITE_DEFAULT_CUSTOMER_ID=customer-1
```

The Vite dev server proxies `/api` to `http://localhost:8080`, so the browser calls the API Gateway without CORS friction.

## Keycloak Users

Use these local users from `ops/keycloak/eventcart-realm.json`:

| User | Password | Roles | Notes |
| --- | --- | --- | --- |
| `customer-user` | `customer` | `CUSTOMER` | Has `customer_id=customer-1` |
| `admin-user` | `admin` | `ADMIN` | Can create products and seed inventory |
| `support-user` | `support` | `SUPPORT` | Can inspect order/payment/notification APIs |

If Keycloak was already running before the React redirect URI was added, recreate the Keycloak container so the realm import is refreshed:

```powershell
docker compose rm -sf keycloak
docker compose up -d keycloak
```

## Run Locally

Start infrastructure and backend services first:

```powershell
cd C:\Users\HP\Documents\Study\EventCart
docker compose up -d
.\mvnw.cmd -pl services/catalog-service spring-boot:run
.\mvnw.cmd -pl services/cart-service spring-boot:run
.\mvnw.cmd -pl services/order-service spring-boot:run
.\mvnw.cmd -pl services/inventory-service spring-boot:run
.\mvnw.cmd -pl services/payment-service spring-boot:run
.\mvnw.cmd -pl services/notification-service spring-boot:run
.\mvnw.cmd -pl services/api-gateway spring-boot:run
```

Then run the UI:

```powershell
cd C:\Users\HP\Documents\Study\EventCart\frontend\eventcart-ui
npm.cmd run dev
```

Open:

```text
http://localhost:5173
```

## Recommended Demo Flow

1. Sign in as `admin-user/admin`.
2. Open Admin and create a product.
3. Seed inventory for the created product.
4. Sign out and sign in as `customer-user/customer`.
5. Open Catalog and add the product to cart.
6. Open Cart and place an order.
7. Open Orders and watch status change from `CREATED` to inventory/payment status.
8. Open Notifications and verify customer notifications.

## Debugging From Developer Point Of View

Use browser DevTools:

- Network tab: verify requests go to `/api/v1/...` and return the shared `ApiResponse` wrapper.
- Request headers: verify `Authorization: Bearer <token>` and `X-Correlation-Id`.
- Application tab: inspect Keycloak session storage if login behaves strangely.
- Console tab: inspect React/Vite runtime errors.

Use backend tools:

- API Gateway logs: verify route/security decisions.
- Service logs: follow the same `X-Correlation-Id`.
- MongoDB: verify product, cart, order, inventory, payment, notification, and outbox collections.
- Redis: verify order idempotency keys.
- Kafka: verify topic messages and DLQ messages.
- Grafana/Prometheus: verify service health and request metrics.

Common local UI symptoms:

| Symptom | Meaning |
| --- | --- |
| `Error while checking login iframe` | Keycloak's local-dev login iframe check can fail on localhost. The UI disables that iframe check and still refreshes tokens normally. Hard-refresh the page after pulling this fix. |
| Product search shows `502` | API Gateway or catalog-service is not reachable. Start `api-gateway` on `8080` and `catalog-service` on `8081`, then retry. |

## Useful Commands

```powershell
npm.cmd run dev
npm.cmd run build
npm.cmd test
npm.cmd run lint
```

## Interview Concepts Covered

Be ready to explain:

- React component composition.
- Props vs state.
- Controlled forms.
- React Hook Form and why it avoids excessive re-renders.
- Zod validation and type inference.
- React Router page routing and protected routes.
- Context API for authentication state.
- Zustand for small global client state.
- React Query for server state, caching, invalidation, polling, and retry.
- Why server state should not be manually duplicated in component state.
- How JWT tokens are attached to API requests.
- Why the browser calls the API Gateway instead of every microservice directly.
- How frontend polling fits an eventually consistent Kafka workflow.
