# notification-service

Notification-service records customer-facing notifications created from order and payment events.

## What This Module Does

- Consumes `OrderCreatedEvent`, `InventoryReservationFailedEvent`, `PaymentCompletedEvent`, and `PaymentFailedEvent`.
- Stores notification records in MongoDB collection `notifications`.
- Exposes APIs to list customer notifications, fetch a notification, and mark a notification as read.
- Uses Kafka retry and DLQ support for failed event handling.
- Uses JWT/Keycloak security through `common-security`.

## Main Functionality

- Order placed notification after order-service stores an order-created event.
- Inventory failure notification when stock cannot be reserved.
- Payment success notification when payment-service completes payment.
- Payment failure notification when payment-service declines payment.

## Local API

- `GET /api/v1/notifications/customers/{customerId}`
- `GET /api/v1/notifications/{notificationId}`
- `PUT /api/v1/notifications/{notificationId}/read`

Swagger UI is available at:

```text
http://localhost:8086/swagger-ui.html
```

## Interview Angle

This service demonstrates event-driven read-side behavior. It does not call order-service or payment-service directly; it reacts to Kafka events and creates its own MongoDB projection.
