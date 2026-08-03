# ADR-0003: Use Redis For Order Idempotency

## Status

Accepted.

## Context

Order placement is sensitive to duplicate requests. A customer or frontend client may retry because of:

- Network timeout.
- Browser refresh.
- Mobile app retry.
- Gateway timeout.
- User double-click.

Without idempotency, the same cart could create duplicate orders.

## Decision

Use Redis in order-service to store idempotency keys for order placement.

Key pattern:

```text
eventcart:orders:idempotency:<client-idempotency-key>
```

Values:

| Value | Meaning |
| --- | --- |
| `IN_PROGRESS` | A request with this key is currently being processed. |
| `ORDER:<order-id>` | A request with this key already completed and created an order. |

Default TTL:

```text
30 minutes
```

## Consequences

Positive:

- Redis `SETNX` style behavior is fast and atomic.
- Duplicate client retries can safely return the original order.
- Temporary idempotency state does not permanently grow in MongoDB.
- The pattern is common in production checkout and payment workflows.

Trade-offs:

- Redis must be available for idempotent order placement.
- TTL selection matters.
- Redis is not the source of truth for orders.
- The service must clear `IN_PROGRESS` keys when order creation fails before completion.

## Alternatives Considered

| Alternative | Reason Not Chosen |
| --- | --- |
| MongoDB unique index on idempotency key | Valid option, but less focused for short-lived retry state. |
| No idempotency | Unsafe for checkout. |
| Client-only duplicate prevention | Not reliable because clients can crash, retry, or be bypassed. |

## Interview Explanation

"We use Redis to protect order creation from duplicate client retries. The client sends an idempotency key. Order-service reserves that key in Redis with a TTL, creates the order, then stores the created order ID against the same key. If the same key is used again, order-service returns the existing order instead of creating another one."
