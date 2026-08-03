# Architecture Documentation

This folder explains EventCart from a system-design point of view. Use these documents when onboarding a new developer, explaining the project in an interview, or debugging a full order flow across services.

## Documents

| Document | Purpose |
| --- | --- |
| [System Overview](system-overview.md) | High-level service map, client entry point, infrastructure, and data ownership. |
| [Service Responsibilities](service-responsibilities.md) | What each service owns, which APIs it exposes, and which technologies it uses. |
| [Kafka Event Flow](kafka-event-flow.md) | Order, inventory, payment, notification, retry, and DLQ event flow. |
| [Deployment View](deployment-view.md) | Local Docker Compose view and Kubernetes-oriented deployment view. |

## How To Use This Folder

1. Start with [System Overview](system-overview.md).
2. Read [Service Responsibilities](service-responsibilities.md) before changing any service.
3. Use [Kafka Event Flow](kafka-event-flow.md) when debugging async behavior.
4. Use [Deployment View](deployment-view.md) when running locally, building images, or preparing Kubernetes deployment.

These documents are intentionally practical. They describe the application as it exists today, not an abstract ideal architecture.
