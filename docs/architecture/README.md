# Architecture Documentation

This folder explains EventCart from a system-design point of view. Use these documents when onboarding a new developer, explaining the project in an interview, or debugging a full order flow across services.

## Documents

| Document | Purpose |
| --- | --- |
| [System Overview](system-overview.md) | High-level service map, client entry point, infrastructure, and data ownership. |
| [Service Responsibilities](service-responsibilities.md) | What each service owns, which APIs it exposes, and which technologies it uses. |
| [Kafka Event Flow](kafka-event-flow.md) | Order, inventory, payment, notification, retry, and DLQ event flow. |
| [Detailed Application Flow](detailed-application-flow.md) | Confluence-style end-to-end business process with sequence, state, and debug diagrams. |
| [Kafka Process Details](kafka-process-details.md) | Producer, consumer, retry, DLQ, topic ownership, and debugging flow. |
| [MongoDB Process Details](mongodb-process-details.md) | Database ownership, document lifecycle, snapshots, outbox, and verification flow. |
| [Redis Process Details](redis-process-details.md) | Order idempotency state machine, duplicate request handling, and Redis debugging. |
| [Frontend UI Flow](frontend-ui-flow.md) | React, Keycloak, API Gateway, and customer shopping flow from the browser. |
| [Deployment View](deployment-view.md) | Local Docker Compose view and Kubernetes-oriented deployment view. |

## How To Use This Folder

1. Start with [System Overview](system-overview.md).
2. Read [Service Responsibilities](service-responsibilities.md) before changing any service.
3. Use [Detailed Application Flow](detailed-application-flow.md) to understand the full customer journey.
4. Use [Kafka Event Flow](kafka-event-flow.md) and [Kafka Process Details](kafka-process-details.md) when debugging async behavior.
5. Use [MongoDB Process Details](mongodb-process-details.md) and [Redis Process Details](redis-process-details.md) when verifying state.
6. Use [Frontend UI Flow](frontend-ui-flow.md) when testing the platform through the React console.
7. Use [Deployment View](deployment-view.md) when running locally, building images, or preparing Kubernetes deployment.

These documents are intentionally practical. They describe the application as it exists today, not an abstract ideal architecture.
