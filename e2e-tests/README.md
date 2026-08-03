# e2e-tests

`e2e-tests` contains Docker-backed platform tests that exercise multiple EventCart services together.

## What This Module Does

- Starts MongoDB, Kafka, and Redis with Testcontainers.
- Creates the Kafka business topics and dead-letter topics before service startup so listeners receive all keyed events deterministically.
- Launches the bootable service jars for catalog, cart, order, inventory, payment, and notification services.
- Drives the real customer flow through HTTP APIs.
- Verifies that order creation flows through Kafka to inventory reservation, payment simulation, order status updates, and notification projection.
- Covers negative platform behavior for empty carts, insufficient inventory, payment failure, duplicate order retries, and Kafka retry/DLQ routing.

## How To Run

Docker Desktop must be running:

```powershell
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
```

When Docker is not available, the Testcontainers test is skipped by JUnit.

## Kafka Topic Setup

The E2E test creates `eventcart.orders.created`, `eventcart.inventory.reserved`, `eventcart.inventory.failed`, `eventcart.payments.completed`, `eventcart.payments.failed`, and their `.dlq` topics before launching the services. This prevents Kafka from auto-creating a topic with the broker default partition count when the first consumer subscribes.

## Covered Scenarios

- Happy path: product, cart, order, inventory reservation, payment success, final order status, and notifications.
- Empty cart: order placement returns `EMPTY_CART` and no order is created.
- Insufficient inventory: order becomes `INVENTORY_FAILED`, reservation is `FAILED`, payment is not created, and cart remains populated.
- Payment failure: payment becomes `FAILED`, order becomes `PAYMENT_FAILED`, inventory reservation is released, and stock is returned.
- Duplicate idempotency key: the second order request returns the original order ID and does not create a second order.
- Kafka retry/DLQ: a poison `OrderCreated` event is retried and then routed to `eventcart.orders.created.dlq`.

## Interview Angle

This module demonstrates why end-to-end tests are slower but valuable: they catch serialization, broker wiring, service startup, configuration, and real HTTP/API integration issues that unit tests cannot see.
