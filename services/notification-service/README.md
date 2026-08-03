# notification-service

Notification-service records customer-facing notifications created from order and payment events.

## What This Module Does

- Consumes `OrderCreatedEvent`, `InventoryReservationFailedEvent`, `PaymentCompletedEvent`, and `PaymentFailedEvent`.
- Stores notification records in MongoDB collection `notifications`.
- Delivers notifications through SMTP email and Twilio-compatible SMS when providers are enabled.
- Exposes APIs to list customer notifications, fetch a notification, and mark a notification as read.
- Uses Kafka retry and DLQ support for failed event handling.
- Uses JWT/Keycloak security through `common-security`.

## Main Functionality

- Order placed notification after order-service stores an order-created event.
- Inventory failure notification when stock cannot be reserved.
- Payment success notification when payment-service completes payment.
- Payment failure notification when payment-service declines payment.
- Optional email and SMS delivery after the notification is persisted.

## Local API

- `GET /api/v1/notifications/customers/{customerId}`
- `GET /api/v1/notifications/{notificationId}`
- `PUT /api/v1/notifications/{notificationId}/read`

Swagger UI is available at:

```text
http://localhost:8086/swagger-ui.html
```

## Provider Configuration

Providers are disabled by default for local learning. To enable real delivery, configure contacts and credentials in `application.yml` or environment variables:

```yaml
eventcart:
  notifications:
    email:
      enabled: true
      from: no-reply@eventcart.local
    sms:
      enabled: true
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      from-number: ${TWILIO_FROM_NUMBER}
    contacts:
      customer-1:
        email: customer1@example.com
        phone-number: "+15555550101"
```

The service stores the notification first, then attempts provider delivery. Provider failures are logged and do not remove the notification history record.

## Interview Angle

This service demonstrates event-driven read-side behavior. It does not call order-service or payment-service directly; it reacts to Kafka events, creates its own MongoDB projection, and can fan out to external notification providers.
