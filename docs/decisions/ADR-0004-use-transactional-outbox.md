# ADR-0004: Use Transactional Outbox

## Status

Accepted.

## Context

Several services need to save local state and publish Kafka events. For example:

- order-service saves an order and publishes `OrderCreated`.
- inventory-service saves a reservation and publishes `InventoryReserved` or `InventoryReservationFailed`.
- payment-service saves a payment attempt and publishes `PaymentCompleted` or `PaymentFailed`.

If a service saves MongoDB state and then crashes before publishing to Kafka, other services will never learn about that state change. If it publishes first and then fails to save state, consumers may react to an event that has no durable source state.

## Decision

Use the transactional outbox pattern:

1. Save the business document.
2. Save an `outbox_events` document in the same service database.
3. Let a scheduler publish pending outbox events to Kafka.
4. Mark the outbox event as `PUBLISHED`.
5. Mark as `FAILED` after repeated publish failures.

## Consequences

Positive:

- Reduces the risk of saved state without a matching event.
- Gives developers a visible place to inspect pending or failed events.
- Event publishing can be retried.
- Keeps event reliability logic inside each service.

Trade-offs:

- Events are published asynchronously, not immediately.
- A scheduler is required.
- Consumers still need idempotency because retries can publish duplicates.
- MongoDB single-document operations are simple, but true multi-document transaction semantics should be considered carefully for production.

## Alternatives Considered

| Alternative | Reason Not Chosen |
| --- | --- |
| Publish directly to Kafka after saving state | Easier, but a crash between save and publish can lose events. |
| Kafka transaction only | Does not automatically include MongoDB state changes. |
| Distributed transaction or 2PC | Too complex and uncommon for this style of microservice architecture. |

## Interview Explanation

"The outbox pattern solves the dual-write problem. Instead of saving to MongoDB and directly publishing to Kafka as two unrelated actions, the service stores an outbox record with the business state. A scheduler publishes that record to Kafka and marks it as published. This improves reliability, but consumers must still be idempotent because duplicate publish attempts can happen."
