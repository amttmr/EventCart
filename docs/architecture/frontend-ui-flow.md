# Frontend UI Flow

This document explains how the React UI connects to the existing EventCart microservices platform.

## Runtime View

```mermaid
flowchart LR
    Browser["Browser\nReact + Vite"] -->|"/api/v1/**"| ViteProxy["Vite dev proxy"]
    ViteProxy --> Gateway["API Gateway\nlocalhost:8080"]
    Browser -->|"OIDC login redirect"| Keycloak["Keycloak\nlocalhost:8088"]
    Gateway --> Catalog["catalog-service\n8081"]
    Gateway --> Cart["cart-service\n8082"]
    Gateway --> Order["order-service\n8083"]
    Gateway --> Inventory["inventory-service\n8084"]
    Gateway --> Payment["payment-service\n8085"]
    Gateway --> Notification["notification-service\n8086"]
```

The React app does not call backend services directly. It calls `/api/v1/...`, Vite proxies the request to the API Gateway, and the gateway routes to the correct backend service.

## Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant React as React UI
    participant Keycloak
    participant Gateway as API Gateway
    participant Service as Backend Service

    User->>React: Click sign in
    React->>Keycloak: Redirect to OIDC login
    Keycloak-->>React: Redirect back with authenticated session
    React->>Keycloak: Exchange/refresh token through keycloak-js
    React->>Gateway: API request with Bearer token
    Gateway->>Gateway: Validate JWT and roles
    Gateway->>Service: Forward request
    Service->>Service: Enforce method security and customer ownership
    Service-->>Gateway: ApiResponse
    Gateway-->>React: ApiResponse
```

Important points:

- `customer-user` has role `CUSTOMER` and claim `customer_id=customer-1`.
- Admin/support users can switch the active customer ID in the UI.
- Customer users are locked to the `customer_id` from the token.
- API Gateway checks roles at the edge.
- Backend services still enforce authorization rules, so security does not depend only on the gateway.

## Customer Shopping Flow From UI

```mermaid
sequenceDiagram
    participant UI as React UI
    participant Gateway as API Gateway
    participant Catalog as catalog-service
    participant Cart as cart-service
    participant Order as order-service
    participant Kafka
    participant Inventory as inventory-service
    participant Payment as payment-service
    participant Notify as notification-service

    UI->>Gateway: GET /api/v1/products
    Gateway->>Catalog: Search products
    Catalog-->>UI: Product page through gateway
    UI->>Gateway: POST /api/v1/carts/{customerId}/items
    Gateway->>Cart: Add productId + quantity
    Cart->>Catalog: Fetch product snapshot
    Cart-->>UI: Updated cart
    UI->>Gateway: POST /api/v1/orders
    Gateway->>Order: Place order with idempotency key
    Order->>Cart: Read customer cart
    Order->>Order: Store order + outbox record
    Order-->>UI: Created order
    Order->>Kafka: Publish OrderCreated from outbox
    Kafka->>Inventory: Consume OrderCreated
    Inventory->>Inventory: Reserve or fail stock
    Inventory->>Kafka: Publish inventory result from outbox
    Kafka->>Payment: Consume InventoryReserved
    Payment->>Payment: Simulate payment
    Payment->>Kafka: Publish payment result from outbox
    Kafka->>Order: Consume final result
    Kafka->>Notify: Consume notification-worthy events
    UI->>Gateway: Poll order/payment/notification APIs
```

The UI uses polling on the Orders and Notifications screens because the backend workflow is asynchronous. This makes eventual consistency visible while you learn Kafka.

## React State Ownership

| State type | Owner | Example |
| --- | --- | --- |
| Server state | React Query | Products, cart, orders, inventory, payments, notifications |
| Auth state | Auth context + Keycloak JS | Token, username, roles, customer ID |
| Small workflow state | Zustand | Active customer ID, selected product ID, last order ID |
| Local form state | React Hook Form | Product create form, inventory form, cart add form |
| Temporary filter state | Component state | Catalog keyword/category filters |

This split is intentional. Server data is cached and invalidated through React Query. UI-only workflow state stays in Zustand. Form state stays close to the form that owns it.

## Debugging Checklist

| Symptom | Check |
| --- | --- |
| Login redirects fail | Confirm Keycloak client allows `http://localhost:5173/*` |
| API returns 401 | Confirm the user is signed in and the token is attached |
| API returns 403 | Confirm the user has the role and customer ownership claim needed |
| Catalog loads but cart fails | Product GET is public, but cart APIs require a customer/admin token |
| Order stays `CREATED` | Check Kafka, inventory-service, and order-service event listener logs |
| Payment is missing | Check payment-service and topic `eventcart.inventory.reserved` |
| UI shows stale data | Use React Query DevTools later or inspect polling/invalidation keys |
| Browser request has no correlation ID | Check `src/lib/apiClient.ts` interceptor |

## Interview Talking Points

- Why use an API Gateway from the browser instead of calling all microservices directly?
- Why does the UI poll order status instead of assuming the order is final immediately?
- What is the difference between frontend client state and backend server state?
- Why is React Query better than manually fetching in every component for this app?
- Why should a customer not be allowed to edit the active customer ID?
- Why is form validation done on the frontend even though the backend also validates?
- How does JWT authentication work in a single-page application?
