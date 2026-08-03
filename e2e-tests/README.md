# e2e-tests

`e2e-tests` contains Docker-backed platform tests that exercise multiple EventCart services together.

## What This Module Does

- Starts MongoDB, Kafka, and Redis with Testcontainers.
- Launches the bootable service jars for catalog, cart, order, inventory, payment, and notification services.
- Drives the real customer flow through HTTP APIs.
- Verifies that order creation flows through Kafka to inventory reservation, payment simulation, order status updates, and notification projection.

## How To Run

Docker Desktop must be running:

```powershell
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
```

When Docker is not available, the Testcontainers test is skipped by JUnit.

## Interview Angle

This module demonstrates why end-to-end tests are slower but valuable: they catch serialization, broker wiring, service startup, configuration, and real HTTP/API integration issues that unit tests cannot see.
