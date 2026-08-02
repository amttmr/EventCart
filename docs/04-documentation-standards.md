# Documentation Standards

Every major feature will be documented as we build it. The documentation should help both development and interview preparation.

## Documentation Types

| Document Type | Location | Purpose |
| --- | --- | --- |
| Setup docs | `docs/` | How to install, run, and verify the project |
| API docs | `docs/api/` and Swagger UI | REST endpoint usage |
| Architecture docs | `docs/architecture/` | System design, diagrams, service boundaries |
| Decision records | `docs/decisions/` | Why we chose a technology or design |
| Interview notes | `docs/interview/` | Questions, answers, explanations, resume points |
| QA handoff guide | `docs/10-qa-application-flow.md` and `docs/EventCart-QA-Application-Flow.docx` | End-to-end API sequence and verification guide for QA and new joiners |
| Troubleshooting | `docs/troubleshooting.md` | Common errors and fixes |

## Feature Documentation Template

Each feature document should include:

```text
# Feature Name

## Purpose

What problem this feature solves.

## User Flow

How the user or system uses this feature.

## API Endpoints

Endpoint, request body, response body, status codes.

## Data Model

MongoDB collection, fields, indexes, sample document.

## Events

Kafka events produced or consumed.

## Failure Handling

Validation errors, retries, duplicate messages, dead-letter behavior.

## Tests

Unit tests, integration tests, Testcontainers coverage.

## Interview Notes

How to explain this feature in interviews.
```

## API Documentation Rule

Every REST API must have:

- Clear endpoint path.
- Request DTO.
- Response DTO.
- Validation rules.
- Success status code.
- Error status codes.
- Example request.
- Example response.

## Kafka Event Documentation Rule

Every Kafka event must have:

- Event name.
- Topic name.
- Producer.
- Consumer or consumers.
- Payload schema.
- Event version.
- Retry behavior.
- Dead-letter behavior.
- Idempotency key.

## Database Documentation Rule

Every MongoDB collection must have:

- Collection name.
- Owning service.
- Sample document.
- Indexes.
- Expected query patterns.
- Retention policy if applicable.

## Interview Documentation Rule

For each completed milestone, we will add:

- Five common interview questions.
- Short answer.
- Detailed answer.
- Mistakes to avoid.
- How this project demonstrates the concept.

## QA Handoff Documentation Rule

Whenever we add or change application functionality, update the QA handoff guide if the change affects:

- API sequence, request payload, response payload, or status code.
- MongoDB database, collection, or verification query.
- Kafka topic, event payload, producer, or consumer.
- Redis key format or idempotency behavior.
- Service startup order, port, local dependency, or troubleshooting steps.

After updating `docs/10-qa-application-flow.md`, regenerate `docs/EventCart-QA-Application-Flow.docx` using:

```powershell
& "C:\Users\HP\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" docs/tools/generate_qa_flow_docx.py docs/10-qa-application-flow.md docs/EventCart-QA-Application-Flow.docx
```
