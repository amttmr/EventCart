# common-test

`common-test` contains reusable testing constants and Testcontainers dependencies shared by service integration tests.

## Responsibility

This module centralizes test support code and infrastructure dependencies that multiple services need.

## Current Functionality

| Class | Purpose |
| --- | --- |
| `TestProfiles` | Shared Spring profile names for tests |

## Current Infrastructure Dependencies

- JUnit Jupiter Testcontainers integration.
- MongoDB Testcontainers.
- Kafka Testcontainers.

The first Kafka/Mongo end-to-end test is `notification-service` `EventCartKafkaE2EIT`. It publishes events to real Kafka and verifies notification projection in real MongoDB when Docker is available.

## Planned Functionality

- Redis Testcontainers setup.
- Reusable test data builders.
- Integration test base classes for multi-service flows.

## Interview Angle

You should be able to explain why integration tests with real infrastructure are valuable for Kafka and MongoDB. Mock-based tests are fast, but they do not catch serialization, indexing, container wiring, or broker behavior issues.
