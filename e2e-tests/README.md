# e2e-tests

`e2e-tests` contains Docker-backed platform tests that exercise multiple EventCart services together.

## What This Module Does

- Starts MongoDB, Kafka, and Redis with Testcontainers.
- Creates the Kafka business topics and dead-letter topics before service startup so listeners receive all keyed events deterministically.
- Launches the bootable service jars for catalog, cart, order, inventory, payment, and notification services.
- Drives the real customer flow through HTTP APIs.
- Verifies that order creation flows through Kafka to inventory reservation, payment simulation, order status updates, and notification projection.

## How To Run

Docker Desktop must be running:

```powershell
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
```

When Docker is not available, the Testcontainers test is skipped by JUnit.

## Kafka Topic Setup

The E2E test creates `eventcart.orders.created`, `eventcart.inventory.reserved`, `eventcart.inventory.failed`, `eventcart.payments.completed`, `eventcart.payments.failed`, and their `.DLT` topics before launching the services. This prevents Kafka from auto-creating a topic with the broker default partition count when the first consumer subscribes.

## Interview Angle

This module demonstrates why end-to-end tests are slower but valuable: they catch serialization, broker wiring, service startup, configuration, and real HTTP/API integration issues that unit tests cannot see.
