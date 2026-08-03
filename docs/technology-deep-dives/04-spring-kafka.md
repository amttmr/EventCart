# Spring Kafka

Spring Kafka is the Java integration layer that lets EventCart services publish to and consume from Apache Kafka using Spring configuration, `KafkaTemplate`, `@KafkaListener`, serializers, retry handlers, and topic declarations.

## Where It Is Used

Spring Kafka code exists mainly in:

- `services/order-service/src/main/java/com/eventcart/order/config`
- `services/order-service/src/main/java/com/eventcart/order/event`
- `services/inventory-service/src/main/java/com/eventcart/inventory/config`
- `services/inventory-service/src/main/java/com/eventcart/inventory/event`
- `services/payment-service/src/main/java/com/eventcart/payment/config`
- `services/payment-service/src/main/java/com/eventcart/payment/event`
- `services/notification-service/src/main/java/com/eventcart/notification/config`
- `services/notification-service/src/main/java/com/eventcart/notification/event`
- `common/common-kafka/src/main/java/com/eventcart/common/kafka`

## Why It Is Used

Spring Kafka reduces boilerplate for:

- Creating producers through `KafkaTemplate`.
- Creating listener containers through `@KafkaListener`.
- Configuring JSON serialization and deserialization.
- Creating topics using `NewTopic`.
- Handling retries and DLQ publishing through `DefaultErrorHandler`.
- Integrating with Spring dependency injection and configuration.

## Producer Flow In EventCart

EventCart uses outbox publishers:

1. Business service stores state in MongoDB.
2. Business service stores an outbox event document in MongoDB.
3. Scheduled outbox publisher reads pending records.
4. Publisher converts JSON payload back to event object.
5. `KafkaTemplate` publishes to the configured topic.
6. Outbox record becomes `PUBLISHED` or remains retryable.

Examples:

- `OrderEventPublisher`
- `InventoryEventPublisher`
- `PaymentEventPublisher`

## Consumer Flow In EventCart

Consumers use `@KafkaListener`:

- inventory-service consumes `OrderCreatedEvent`.
- order-service consumes inventory and payment result events.
- payment-service consumes `InventoryReservedEvent`.
- inventory-service consumes `PaymentFailedEvent` for compensation.
- notification-service consumes order, inventory failure, and payment result events.

Listeners copy correlation IDs from event metadata into MDC so logs can be traced across services.

## Retry And DLQ

Common retry configuration lives in:

```text
common/common-kafka/src/main/java/com/eventcart/common/kafka/KafkaDeadLetterSupport.java
```

The shared `DefaultErrorHandler`:

- Retries failed records with a fixed backoff.
- Publishes exhausted records to `<original-topic>.dlq`.
- Preserves the original partition.

The E2E suite verifies this by publishing a poison `OrderCreated` event and checking `eventcart.orders.created.dlq`.

## Best Practices

- Configure producers and consumers explicitly for learning clarity.
- Use strong event types instead of raw maps in application code.
- Use `StringSerializer` for keys and JSON serializer for values.
- Use trusted packages carefully for JSON deserialization.
- Use one consumer group per logical service.
- Keep listener methods small and delegate business logic to services.
- Make listener business logic idempotent.
- Use retry for transient failures and DLQ for poison messages.
- Log event ID, order ID, topic, and correlation ID.
- Use the outbox pattern for reliable publishing after database updates.
- Avoid doing long blocking work inside Kafka listener threads.

## How To Verify Behavior

Run Spring Kafka unit and integration tests:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -P integration-tests verify
```

Run full E2E:

```powershell
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
```

Expected E2E coverage:

- `OrderCreated` is published.
- inventory-service consumes it.
- inventory-service publishes inventory result.
- payment-service consumes reservation success.
- payment-service publishes payment result.
- order-service consumes final result.
- poison message goes to `.dlq`.

Verify service logs:

```powershell
Get-Content e2e-tests\target\service-logs\order-service.log -Tail 120
Get-Content e2e-tests\target\service-logs\inventory-service.log -Tail 120
Get-Content e2e-tests\target\service-logs\payment-service.log -Tail 120
```

## How To Debug

Enable useful logs temporarily:

```yaml
logging:
  level:
    org.springframework.kafka: DEBUG
    org.apache.kafka.clients.consumer: INFO
    org.apache.kafka.clients.producer: INFO
```

Debug checklist:

| Problem | Check |
| --- | --- |
| Producer cannot publish | Verify `spring.kafka.bootstrap-servers`. |
| Listener not receiving | Verify topic name, group ID, and consumer assignment. |
| Deserialization error | Check event class, JSON shape, type headers, and trusted package settings. |
| Message retries forever | Check retry max attempts and whether exception is transient. |
| DLQ empty | Check that the listener actually throws and that the `.dlq` topic exists. |
| Duplicate processing | Confirm consumer logic checks existing records by order ID or event ID. |

## Developer Verification Points

When adding a new event:

1. Add event record in `common-events`.
2. Add topic property in producer and consumer services.
3. Add `NewTopic` declaration.
4. Add producer method.
5. Add listener method.
6. Add retry/DLQ configuration if consumed.
7. Add unit tests.
8. Add E2E or integration test for the event path.
9. Document the topic in `docs/08-event-driven-order-inventory-flow.md`.

## Interview Preparation

You should be able to explain:

- What `KafkaTemplate` does.
- What `@KafkaListener` does.
- How Spring creates listener containers.
- Difference between Kafka itself and Spring Kafka.
- How JSON serialization/deserialization is configured.
- What a consumer group ID means.
- How Spring Kafka retry and DLQ work.
- Why EventCart uses outbox publishers instead of publishing directly inside the request transaction.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is `KafkaTemplate`? | A Spring abstraction for sending records to Kafka. |
| What is `@KafkaListener`? | An annotation that creates a listener container to poll Kafka and invoke a method for each record. |
| How do you handle listener failure? | Retry transient failures and route exhausted records to a DLQ. |
| Why use explicit config? | It makes producer, consumer, serialization, retry, and topic behavior visible and interview-friendly. |
| How do you avoid duplicate side effects? | Make consumers idempotent by checking existing records before writing. |

## EventCart Takeaway

Spring Kafka turns Kafka concepts into maintainable Spring code. In EventCart it demonstrates typed events, explicit producers and consumers, retry/DLQ, outbox publishing, idempotent consumers, and testable asynchronous workflows.

