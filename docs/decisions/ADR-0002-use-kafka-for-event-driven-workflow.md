# ADR-0002: Use Kafka For Event-Driven Order Workflow

## Status

Accepted.

## Context

After an order is created, several things must happen:

- Inventory must reserve stock.
- Payment must be processed.
- Order status must be updated.
- Notifications must be created.
- Inventory must be released if payment fails.

These steps do not all need to happen inside the original HTTP request. They cross service boundaries and can fail independently.

## Decision

Use Apache Kafka for asynchronous business events:

| Event | Topic |
| --- | --- |
| Order created | `eventcart.orders.created` |
| Inventory reserved | `eventcart.inventory.reserved` |
| Inventory failed | `eventcart.inventory.failed` |
| Payment completed | `eventcart.payments.completed` |
| Payment failed | `eventcart.payments.failed` |

## Consequences

Positive:

- Services are loosely coupled.
- Order creation can return before inventory, payment, and notification complete.
- Consumers can scale independently through consumer groups.
- Failures can be retried and moved to DLQ topics.
- The architecture demonstrates real microservice event choreography.

Trade-offs:

- Eventual consistency must be explained to users and developers.
- Duplicate events are possible, so consumers must be idempotent.
- Debugging requires checking logs, topics, consumer groups, MongoDB state, and DLQs.
- Event contract changes need care.

## Alternatives Considered

| Alternative | Reason Not Chosen |
| --- | --- |
| Synchronous HTTP calls for every step | Simpler to understand, but creates tight coupling and long request latency. |
| RabbitMQ | Good queueing option, but Kafka is widely used for event streaming and consumer-group based processing. |
| In-process events | Not suitable for multiple independently deployed services. |

## Interview Explanation

"Kafka is used because order processing is a distributed workflow. Order-service should not synchronously control inventory, payment, and notification. Instead, each service reacts to events, updates its own state, and publishes the next event. This is a choreography-based saga. The trade-off is eventual consistency and the need for idempotency, retries, DLQs, and monitoring."
