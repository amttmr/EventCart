# common-kafka

Shared Kafka helper module for EventCart services.

## What This Module Does

- Creates reusable Kafka retry and dead-letter-topic support.
- Sends exhausted consumer records to `<original-topic>.dlq`.
- Keeps retry behavior consistent across order, inventory, payment, and notification consumers.

## Why It Exists

Kafka consumers should not silently lose messages when processing fails. The retry/DLQ pattern lets the application retry transient failures and preserve poison messages for debugging.
