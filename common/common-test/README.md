# common-test

`common-test` contains reusable testing constants and, later, shared Testcontainers helpers.

## Responsibility

This module will centralize test support code that multiple services need.

## Current Functionality

| Class | Purpose |
| --- | --- |
| `TestProfiles` | Shared Spring profile names for tests |

## Planned Functionality

- MongoDB Testcontainers setup.
- Kafka Testcontainers setup.
- Redis Testcontainers setup.
- Reusable test data builders.
- Integration test base classes.

## Interview Angle

You should be able to explain why integration tests with real infrastructure are valuable for Kafka and MongoDB. Mock-based tests are fast, but they do not catch serialization, indexing, container wiring, or broker behavior issues.

