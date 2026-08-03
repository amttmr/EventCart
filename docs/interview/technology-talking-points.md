# Technology Talking Points

This document gives concise interview-ready explanations for each major EventCart technology.

## Java 25

EventCart targets Java 25. The project uses Maven compiler release `25`, and CI uses Java 25 through `actions/setup-java`.

Talking point:

"I aligned local development, CI, Docker images, and Maven compiler settings to Java 25 so the same version is used across the build lifecycle."

## Spring Boot

Spring Boot is the application framework for all backend services. It provides REST support, validation, dependency injection, configuration, Actuator, MongoDB, Kafka, Redis, and security integration.

Talking point:

"Spring Boot lets each service focus on business logic while still providing production concerns such as health checks, metrics, validation, and externalized configuration."

## MongoDB And Spring Data MongoDB

MongoDB stores service-owned documents. Spring Data MongoDB maps Java documents to collections and provides repositories, indexes, auditing, and optimistic locking.

Talking point:

"MongoDB fits the aggregate model for carts and orders. Spring Data MongoDB gives repository-based access while keeping the service responsible for its own document model."

## Kafka And Spring Kafka

Kafka carries domain events between services. Spring Kafka provides `KafkaTemplate`, `@KafkaListener`, serializers, retry handling, DLQ publishing, and topic declarations.

Talking point:

"Kafka is used where the workflow can be asynchronous. Order-service does not directly control inventory or payment; services react to events and publish the next event."

## Redis

Redis stores temporary idempotency keys for order placement.

Talking point:

"Redis is used for fast atomic retry protection. It is not the order source of truth; MongoDB owns orders."

## Keycloak And Spring Security

Keycloak issues JWTs. Gateway and backend services validate JWTs using Spring Security OAuth2 Resource Server. Roles and customer ownership checks enforce authorization.

Talking point:

"Security is implemented as defense in depth: gateway validates tokens, backend services validate tokens, and customer-owned resources check the token's `customer_id` claim."

## Transactional Outbox

Outbox records are stored in MongoDB before Kafka publication.

Talking point:

"Outbox reduces the dual-write problem. We save the business state and an outbox event, then a scheduler publishes pending records to Kafka."

## Testcontainers

Testcontainers runs real MongoDB, Kafka, and Redis containers during integration and E2E tests.

Talking point:

"Testcontainers catches issues that mocks miss, such as serialization, Kafka topic setup, MongoDB connection behavior, and full workflow timing."

## Observability

Actuator exposes health and metrics. Micrometer records application metrics. Prometheus scrapes metrics. Grafana visualizes dashboards. OpenTelemetry captures traces. Correlation IDs connect logs.

Talking point:

"Observability makes distributed debugging possible. I can follow a request through logs, inspect metrics in Prometheus/Grafana, and use traces to understand cross-service latency."

## Docker, GitHub Actions, And Kubernetes

Docker packages each service. GitHub Actions builds, tests, and verifies Docker images. Kubernetes manifests show how services can be deployed with config, secrets, services, and deployments.

Talking point:

"The project is not only runnable locally. It has a path from source code to tested Docker images and Kubernetes-style deployment."
