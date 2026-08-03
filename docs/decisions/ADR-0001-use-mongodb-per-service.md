# ADR-0001: Use MongoDB With Database-Per-Service Ownership

## Status

Accepted.

## Context

EventCart stores data for products, carts, orders, inventory reservations, payment attempts, notifications, and outbox events. The system is intentionally built as microservices, so each service should own its data and avoid direct writes into another service's database.

Most data in this project is aggregate-oriented:

- A cart contains embedded cart items.
- An order contains an order item snapshot.
- A payment attempt belongs to an order.
- A notification is a customer-facing event projection.

## Decision

Use MongoDB as the primary database and give each service its own database:

| Service | Database |
| --- | --- |
| catalog-service | `eventcart_catalog` |
| cart-service | `eventcart_cart` |
| order-service | `eventcart_order` |
| inventory-service | `eventcart_inventory` |
| payment-service | `eventcart_payment` |
| notification-service | `eventcart_notification` |

## Consequences

Positive:

- Document models fit carts, order snapshots, and notification records naturally.
- Each service owns its persistence model.
- Schema can evolve independently per service.
- MongoDB works well with Spring Data repositories and Testcontainers.

Trade-offs:

- No cross-service joins.
- Cross-service consistency must be handled through APIs or events.
- Developers must design indexes intentionally.
- Reporting queries may need a separate read model or analytics pipeline.

## Alternatives Considered

| Alternative | Reason Not Chosen |
| --- | --- |
| Single shared relational database | Easier at first, but violates service ownership and creates tight coupling. |
| PostgreSQL database per service | Strong option, but document-shaped cart/order data makes MongoDB easier for this learning project. |
| One shared MongoDB database | Simpler locally, but weaker service ownership boundary. |

## Interview Explanation

"We used MongoDB because the data is naturally document-shaped and each microservice can own its own database. Cart and order snapshots are good examples because they contain embedded items that are usually loaded together. The trade-off is that we do not rely on joins across services. Cross-service workflow is handled through APIs and Kafka events."
