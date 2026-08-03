# React UI Interview Guide

This guide connects the EventCart React UI to interview topics you should be able to explain.

## Project Explanation

EventCart UI is a React and TypeScript single-page application that acts as the frontend console for a microservices-based e-commerce workflow. It authenticates users with Keycloak, calls the Spring Cloud API Gateway, uses React Query for backend server state, and lets users create products, seed inventory, add items to cart, place orders, inspect Kafka-driven order status, and read notifications.

## Technologies And Why They Are Used

| Technology | Why it is used |
| --- | --- |
| React | Builds reusable UI components for catalog, cart, orders, notifications, and admin flows |
| TypeScript | Prevents common API and prop-shape mistakes at compile time |
| Vite | Gives fast local development and production builds |
| React Router | Keeps each business area in its own route and supports protected pages |
| React Query | Manages API loading/error state, caching, polling, invalidation, and retry |
| Zustand | Stores small cross-page workflow state without heavy boilerplate |
| Keycloak JS | Handles browser login, logout, token parsing, and token refresh |
| Axios | Central HTTP client with base URL, timeout, token, and correlation ID interceptor |
| React Hook Form | Efficient form handling for product, inventory, and cart operations |
| Zod | Runtime validation plus TypeScript inference for form values |
| Lucide React | Consistent icon controls and navigation |
| Vitest | Fast unit tests for frontend utility functions |
| Oxlint | Fast lint feedback for TypeScript/React mistakes |

## Core React Concepts In This UI

Component composition:

- `Layout` owns the shell, navigation, customer selector, and auth summary.
- Pages own business workflows.
- Small reusable components handle loading, empty, error, and status states.

Props:

- Components such as `StatusBadge`, `ErrorBanner`, and `EmptyState` receive narrow props.
- This keeps components reusable and easy to test.

State:

- Local component state handles filters and selected IDs inside one screen.
- Zustand handles small state that crosses pages.
- React Query handles server state.

Effects:

- `AuthProvider` uses effects to initialize Keycloak and refresh tokens.
- `Layout` uses an effect to keep customer users locked to their token customer ID.

Forms:

- React Hook Form keeps form state outside React's normal render path.
- Zod validates input before the mutation calls the backend.
- Backend validation still remains required because frontend validation can be bypassed.

## React Query Concepts

EventCart uses React Query because most UI state is server-owned.

Examples:

- Catalog products are fetched with `useQuery`.
- Cart mutations invalidate cart queries.
- Order tracking polls every few seconds because Kafka processing is asynchronous.
- Notification read mutations invalidate notification queries.

Interview answer:

```text
React Query is not just a fetch wrapper. It is a server-state cache. It gives us loading/error states, retry, caching, invalidation, and polling. In EventCart, that matters because order status changes asynchronously after Kafka consumers process inventory and payment events.
```

## Authentication And Authorization

The UI uses Keycloak JS to obtain a JWT token. Axios attaches the token to protected API calls. The gateway validates the token and checks roles. Backend services still enforce role and customer ownership rules.

Interview answer:

```text
The React app handles authentication, but it does not make authorization decisions trustworthy. The backend owns authorization. The UI hides or disables actions for usability, while the gateway and services enforce the real rules.
```

## Server State Vs Client State

| State | Location | Reason |
| --- | --- | --- |
| Products | React Query | Backend-owned and cacheable |
| Cart | React Query | Backend-owned and changed by mutations |
| Orders | React Query | Backend-owned and updated asynchronously |
| Notifications | React Query | Backend-owned and polled |
| Active customer ID | Zustand | Small cross-page UI workflow state |
| Form values | React Hook Form | Owned by a specific form |
| Auth token | Auth context/Keycloak | Shared by all API requests |

Interview answer:

```text
I avoid copying backend data into global state. React Query owns server state because it knows when to refetch and invalidate. Zustand is reserved for tiny UI workflow state that is not a backend resource.
```

## Common Interview Questions

What is a SPA?

A single-page application loads one HTML page and updates views on the client using JavaScript routing. EventCart UI uses React Router to switch between dashboard, catalog, cart, orders, notifications, and admin screens.

Why use TypeScript with React?

TypeScript catches prop, API response, and form-shape mistakes before runtime. In EventCart, API DTO types make it clear which fields come from catalog, cart, order, inventory, payment, and notification services.

Why use React Query instead of `useEffect` plus `fetch` everywhere?

Manual fetching repeats loading/error/cache/refetch logic in every component. React Query centralizes server-state behavior and supports invalidation after mutations.

What is a protected route?

A protected route checks whether the user is authenticated and has required roles before rendering a page. EventCart uses protected routes for cart, orders, notifications, and admin screens.

What is a controlled component?

A controlled component stores its current value in React state and updates it through event handlers. EventCart uses controlled inputs for catalog filters and order lookup.

Why use React Hook Form?

It reduces unnecessary re-renders and gives a clean API for validation and submission. This is useful for forms such as product creation and inventory seeding.

Why use Zod?

Zod validates data at runtime and infers TypeScript types. EventCart uses it to validate form input before calling backend APIs.

Why use Zustand?

Zustand is lightweight and useful for small shared client state. EventCart uses it for active customer ID, selected product ID, and last order ID.

How does the UI handle eventual consistency?

The Orders screen polls order, inventory reservation, and payment APIs. This makes Kafka-driven asynchronous updates visible to the user.

How would you improve the UI later?

Add WebSocket/SSE updates for order status, React Query DevTools, better accessibility tests, component tests, Docker image publishing for the UI, and production deployment behind the same gateway or CDN.

## Resume Bullets

- Built a React 19 + TypeScript frontend for a Spring Boot microservices e-commerce platform.
- Integrated Keycloak login with protected React Router routes and JWT-backed API Gateway calls.
- Used TanStack React Query for cached server state, mutations, invalidation, polling, and retry.
- Built catalog, cart, order tracking, inventory admin, and notification workflows against live backend APIs.
- Added Zod and React Hook Form validation for product, inventory, and cart forms.
- Added a centralized Axios client with bearer token injection, timeout handling, and correlation IDs.
