# Architecture Decision Records

This folder contains ADRs, meaning Architecture Decision Records. An ADR captures an important technical choice, the context behind it, and the consequences of that choice.

## ADR Format

Each ADR uses this structure:

- Status
- Context
- Decision
- Consequences
- Alternatives considered
- Interview explanation

## Current Decisions

| ADR | Decision |
| --- | --- |
| [ADR-0001](ADR-0001-use-mongodb-per-service.md) | Use MongoDB with database-per-service ownership. |
| [ADR-0002](ADR-0002-use-kafka-for-event-driven-workflow.md) | Use Kafka for asynchronous order workflow. |
| [ADR-0003](ADR-0003-use-redis-for-order-idempotency.md) | Use Redis for order idempotency keys. |
| [ADR-0004](ADR-0004-use-transactional-outbox.md) | Use transactional outbox for reliable event publishing. |
| [ADR-0005](ADR-0005-use-keycloak-and-resource-server-security.md) | Use Keycloak and Spring Security OAuth2 Resource Server. |

## Why ADRs Matter

ADRs help new developers and interviewers understand that the project choices were intentional. They also make trade-offs visible. A good architecture answer is rarely "we used Kafka because it is popular"; it should explain the problem, the constraint, the selected approach, and what new complexity the decision introduced.
