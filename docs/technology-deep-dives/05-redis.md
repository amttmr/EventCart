# Redis

Redis is the in-memory key-value store used by EventCart order-service for order-placement idempotency.

## Where It Is Used

Redis runs locally through [compose.yaml](../../compose.yaml) as `eventcart-redis` on port `6379`.

Application code:

- `services/order-service/src/main/java/com/eventcart/order/config/RedisConfig.java`
- `services/order-service/src/main/java/com/eventcart/order/config/OrderRedisProperties.java`
- `services/order-service/src/main/java/com/eventcart/order/service/OrderIdempotencyService.java`

Key format:

```text
eventcart:orders:idempotency:<client-idempotency-key>
```

Possible values:

```text
IN_PROGRESS
ORDER:<order-id>
```

Default TTL:

```text
30 minutes
```

## Why Redis Is Used

Order placement is vulnerable to duplicate requests:

- User double-clicks checkout.
- Browser retries after a timeout.
- Mobile app retries on network failure.
- Gateway/client repeats the same request.

Redis is good for this because it supports fast atomic operations with TTL.

EventCart uses `SETNX` style behavior through `StringRedisTemplate.opsForValue().setIfAbsent(...)`:

1. First request reserves the idempotency key as `IN_PROGRESS`.
2. After order creation succeeds, key is updated to `ORDER:<order-id>`.
3. A later request with the same key returns the original order.
4. A request while the first one is still processing receives duplicate-request behavior.
5. TTL eventually removes old idempotency keys.

## Best Practices

- Always set TTLs for idempotency keys.
- Use atomic write-if-absent operations.
- Namespace keys clearly.
- Store small values, not large objects.
- Return the original result for completed duplicate requests.
- Clear `IN_PROGRESS` markers when the business operation fails.
- Monitor memory usage and evictions.
- Do not use Redis as the source of truth for orders. MongoDB owns orders.
- Keep Redis usage optional for local learning but mandatory for production checkout reliability.

## How To Verify Locally

Start Redis:

```powershell
docker compose up -d redis
docker compose ps redis
```

Connect:

```powershell
docker exec -it eventcart-redis redis-cli
```

List idempotency keys:

```text
KEYS eventcart:orders:idempotency:*
```

Inspect one key:

```text
GET eventcart:orders:idempotency:customer-1-order-20260802-001
TTL eventcart:orders:idempotency:customer-1-order-20260802-001
```

Expected after successful order:

```text
ORDER:<order-id>
```

Expected TTL:

```text
A positive number of seconds
```

## How To Debug

| Symptom | Check |
| --- | --- |
| Duplicate order created | Confirm the client sent the same `idempotencyKey` and Redis was reachable. |
| Duplicate retry returns conflict | The first request may still be `IN_PROGRESS`. |
| Retry does not return old order | Check whether TTL expired or key was never completed. |
| Order failed but key remains | Check order-service logs for `clearIfInProgress`. |
| Redis health is down | Check `docker compose ps redis` and order-service Redis host/port. |

Useful commands:

```text
PING
INFO memory
INFO stats
DBSIZE
TTL <key>
GET <key>
DEL <key>
```

Use `DEL` only in local debugging when you intentionally want to remove a stuck test key.

## Real-Time Monitoring

Local:

- `docker logs eventcart-redis`
- `redis-cli INFO`
- `redis-cli MONITOR` for short local debugging only

Production:

- Memory usage
- Eviction count
- Connected clients
- Command latency
- Keyspace hits and misses
- Expired keys
- CPU usage
- Persistence errors if AOF/RDB is enabled

Do not run `MONITOR` in production for long periods because it is expensive.

## Interview Preparation

You should be able to explain:

- Difference between cache and source of truth.
- Why Redis is used for idempotency.
- What TTL means.
- What atomic `set-if-absent` solves.
- Why order-service stores `ORDER:<order-id>` after success.
- What happens if Redis is unavailable.
- When Redis data loss is acceptable and when it is not.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| Why Redis for idempotency? | It provides fast atomic key reservation with TTL, which prevents duplicate order creation from repeated checkout calls. |
| Why not store idempotency only in MongoDB? | MongoDB could work with unique indexes, but Redis is fast, temporary, and suited for short-lived keys. |
| What is TTL? | Time to live. Redis automatically expires the key after the configured duration. |
| Is Redis the source of truth? | No. MongoDB stores orders. Redis only stores retry/idempotency state. |
| What if Redis crashes? | In this project, duplicate protection can weaken until Redis recovers. Production setups need persistence and high availability if this guarantee is critical. |

## EventCart Takeaway

Redis in EventCart teaches a common production pattern: fast atomic idempotency for checkout. It keeps retries safe without making Redis the owner of order data.

