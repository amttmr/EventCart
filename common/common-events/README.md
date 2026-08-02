# common-events

`common-events` contains shared event contracts used by EventCart services when they communicate through Kafka.

## Responsibility

This module defines the structure of cross-service events. Event classes live here so producers and consumers agree on event names, metadata, payload fields, and versioning.

## Current Functionality

| Class | Purpose |
| --- | --- |
| `EventMetadata` | Common metadata included in every domain event |
| `ProductCreatedEvent` | Event contract for product creation in the catalog domain |

## Why This Module Exists

In an event-driven system, event contracts are part of the public API between services. Keeping them in a shared module helps us learn:

- Event versioning.
- Correlation IDs.
- Event IDs for idempotency.
- Producer-consumer contracts.
- How Kafka messages differ from REST DTOs.

## Interview Angle

You should be able to explain that shared event contracts reduce accidental schema drift in a learning monorepo. In larger organizations, teams may publish event schemas through a schema registry instead of sharing Java classes directly.

