# Learning And Interview Roadmap

This roadmap connects every implementation phase with the concepts you should be able to explain in an interview.

For deeper preparation, use the [Technology Deep Dives](technology-deep-dives/README.md) while moving through this roadmap. Those documents explain where each technology is used in EventCart, how to verify it locally, how to debug it, and how to discuss it in interviews.

## Phase 1: Java And Spring Boot Foundation

What we build:

- Maven multi-module project.
- One working Spring Boot service.
- REST APIs.
- Validation and exception handling.
- OpenAPI documentation.

Interview topics:

- What problem does Spring Boot solve?
- Dependency injection and inversion of control.
- Controller vs service vs repository.
- Bean lifecycle basics.
- Configuration properties.
- REST API design.
- Global exception handling.

## Phase 2: MongoDB With Spring Data

What we build:

- Product catalog documents.
- Repositories and custom queries.
- Indexes.
- Search/filter APIs.

Interview topics:

- SQL vs NoSQL trade-offs.
- Document modeling.
- Embedded vs referenced data.
- Indexing strategy.
- Aggregation pipeline basics.
- Optimistic locking.
- How MongoDB fits product catalogs and order snapshots.

## Phase 3: Kafka And Event-Driven Architecture

What we build:

- Kafka topics.
- Producers and consumers.
- Shared event contracts.
- Order, inventory, and payment events.

Interview topics:

- Kafka topic, partition, offset, broker.
- Consumer groups.
- At-least-once delivery.
- Duplicate messages and idempotency.
- Retry and dead-letter topics.
- Eventual consistency.
- Event schema versioning.

## Phase 4: Distributed Order Workflow

What we build:

- Order creation.
- Inventory reservation.
- Payment simulation.
- Order status updates from Kafka events.

Interview topics:

- Saga pattern.
- Distributed transaction problem.
- Choreography vs orchestration.
- Consistency vs availability.
- How to recover from partial failure.
- Why idempotency matters.

## Phase 5: Security

What we build:

- Keycloak realm.
- OAuth2/JWT resource server setup.
- Role-based authorization.

Interview topics:

- Authentication vs authorization.
- JWT structure.
- OAuth2 resource server.
- Role-based access control.
- Token validation.
- Securing service-to-service calls.

## Phase 6: Testing

What we build:

- Unit tests.
- REST controller tests.
- MongoDB integration tests.
- Kafka integration tests.
- Testcontainers.

Interview topics:

- Unit vs integration tests.
- Mocking vs real dependencies.
- Why Testcontainers is useful.
- Testing asynchronous Kafka flows.
- Test data builders.

## Phase 7: Production Readiness

What we build:

- Docker Compose local stack.
- Actuator health endpoints.
- Prometheus metrics.
- Grafana dashboards.
- Structured logging.
- Correlation IDs.
- OpenTelemetry tracing.

Interview topics:

- Health checks.
- Metrics vs logs vs traces.
- Observability.
- Correlation ID.
- Horizontal scaling concerns.
- Kubernetes readiness and liveness probes.

## Phase 8: Final Interview Package

What we prepare:

- Architecture diagram.
- API documentation.
- Kafka event flow diagram.
- Database design notes.
- Common interview Q&A.
- Resume bullet points.
- Project explanation script.

Interview topics:

- Explain the whole system in 2 minutes.
- Explain one difficult bug or design trade-off.
- Explain why Kafka was used.
- Explain why MongoDB was used.
- Explain how failures are handled.
- Explain how you would scale the system.

## Phase 9: React Frontend

What we build:

- React and TypeScript single-page application.
- Vite local dev workflow.
- Keycloak login from the browser.
- Protected routes by role.
- API Gateway calls with bearer token and correlation ID.
- React Query server state for products, carts, orders, payments, inventory, and notifications.
- Zustand workflow state for active customer, selected product, and last order.
- React Hook Form and Zod validated forms.

Interview topics:

- React component composition.
- Props, state, context, and effects.
- Client state vs server state.
- React Router protected routes.
- JWT authentication in a SPA.
- React Query caching, invalidation, polling, and retries.
- Form validation and why backend validation is still required.
- How frontend polling fits an eventually consistent Kafka workflow.
