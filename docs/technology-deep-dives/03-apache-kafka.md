# Apache Kafka

Apache Kafka is the event broker used by EventCart to connect order, inventory, payment, and notification workflows asynchronously.

## Where It Is Used

Kafka runs locally through [compose.yaml](../../compose.yaml) as `eventcart-kafka` on port `9092`.

EventCart topics:

| Topic | Producer | Consumers |
| --- | --- | --- |
| `eventcart.orders.created` | order-service | inventory-service, notification-service |
| `eventcart.inventory.reserved` | inventory-service | order-service, payment-service |
| `eventcart.inventory.failed` | inventory-service | order-service, notification-service |
| `eventcart.payments.completed` | payment-service | order-service, notification-service |
| `eventcart.payments.failed` | payment-service | order-service, inventory-service, notification-service |
| `<topic>.dlq` | Spring Kafka retry recoverer | Developers, QA, support tools |

## Why Kafka Is Used

Kafka is used because the order workflow is distributed:

1. order-service creates the order.
2. inventory-service reserves stock.
3. payment-service processes payment.
4. notification-service records notifications.
5. order-service updates status as results arrive.

These services should not be tightly coupled through one long synchronous request. Kafka lets one service publish a business fact and lets other services react independently.

## Important Concepts

| Concept | Meaning |
| --- | --- |
| Broker | Kafka server that stores topic partitions. |
| Topic | Named stream of records. |
| Partition | Ordered log segment within a topic. |
| Offset | Position of a record inside a partition. |
| Producer | Application that writes records. |
| Consumer | Application that reads records. |
| Consumer group | Set of consumers sharing work for a topic. |
| Key | Controls partition routing for ordering by aggregate. |
| At-least-once delivery | A record may be delivered more than once. Consumers must be idempotent. |
| DLQ | Dead-letter queue/topic that stores poison messages after retries are exhausted. |

## Event Design In EventCart

Events are business facts:

- `OrderCreatedEvent`: an order exists.
- `InventoryReservedEvent`: stock was reserved.
- `InventoryReservationFailedEvent`: stock could not be reserved.
- `PaymentCompletedEvent`: payment completed.
- `PaymentFailedEvent`: payment failed.

Each event contains metadata:

- `eventId`
- `eventType`
- `eventVersion`
- `correlationId`
- `occurredAt`

This supports traceability, idempotency, and schema evolution.

## Best Practices

- Use business event names, not technical command names.
- Use keys that preserve ordering for one aggregate, such as `orderId`.
- Keep event payloads stable and versioned.
- Include enough snapshot data so consumers avoid unnecessary synchronous calls.
- Assume duplicate delivery and make consumers idempotent.
- Use retries for temporary failures and DLQ for poison messages.
- Avoid publishing directly after database writes without reliability protection. EventCart uses the outbox pattern.
- Keep topic partition count consistent across environments.
- Monitor consumer lag.
- Do not use Kafka for request/response operations that need immediate user feedback.

## How To Verify Locally

Start Kafka:

```powershell
docker compose up -d kafka
docker compose ps kafka
```

List topics:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Describe one topic:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic eventcart.orders.created
```

Read order-created messages:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic eventcart.orders.created --from-beginning --max-messages 5
```

Read payment-failed messages:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic eventcart.payments.failed --from-beginning --max-messages 5
```

Read a DLQ topic:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic eventcart.orders.created.dlq --from-beginning --max-messages 5
```

Check consumer groups:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

Check lag:

```powershell
docker exec -it eventcart-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group inventory-service
```

## How To Debug

| Symptom | Check |
| --- | --- |
| Order stays `CREATED` | Check `eventcart.orders.created`, inventory-service logs, and inventory consumer group lag. |
| Order stays `INVENTORY_RESERVED` | Check payment-service logs and `eventcart.inventory.reserved`. |
| Payment failure did not release stock | Check inventory-service consumption of `eventcart.payments.failed`. |
| Notification missing | Check notification-service group lag and relevant topic. |
| Message appears in `.dlq` | The consumer failed after retry attempts. Inspect payload and service logs. |
| Consumer sees only some messages | Check topic partitions and group assignment. |

Developer log patterns:

- `Publishing OrderCreated event`
- `Outbox event published`
- `Consumed OrderCreated event`
- `Inventory reserved`
- `Payment completed`
- `Payment failed`
- `Dead-letter` or `.dlq`

## Real-Time Monitoring

For local development:

- Use Kafka CLI tools in the container.
- Use service logs.
- Use E2E tests to verify the full event path.

For production:

- Monitor consumer lag per group and topic.
- Monitor broker disk usage.
- Monitor under-replicated partitions.
- Monitor record production and consumption rates.
- Alert on DLQ growth.
- Track retry counts and failed consumer records.

## Interview Preparation

You should be able to explain:

- What a topic, partition, offset, broker, and consumer group are.
- Why Kafka gives ordering only within a partition.
- Why the order ID is a good message key.
- What at-least-once delivery means.
- Why consumers must be idempotent.
- Difference between retry and DLQ.
- Difference between Kafka and HTTP calls.
- How the outbox pattern protects event publishing.
- What consumer lag means.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| Why Kafka instead of REST between order and inventory? | Inventory work can happen asynchronously, and Kafka decouples services while preserving durable events. |
| Does Kafka guarantee exactly-once processing? | Kafka has exactly-once producer/stream features, but application side effects still need idempotency. EventCart assumes at-least-once processing. |
| Why use a key? | The key routes related records to the same partition and preserves ordering for one order. |
| What is consumer lag? | Difference between latest broker offset and committed consumer offset. |
| What is a DLQ? | A topic where failed messages are stored after retries so they can be inspected and replayed later. |

## EventCart Takeaway

Kafka is the backbone of EventCart's asynchronous workflow. It teaches event-driven architecture, partitions, consumer groups, eventual consistency, retries, DLQs, idempotency, and outbox-based reliability.

