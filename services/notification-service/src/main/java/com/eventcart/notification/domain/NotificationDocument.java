package com.eventcart.notification.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document that stores one customer notification.
 */
@Document(collection = "notifications")
public class NotificationDocument {
    @Id
    private String id;

    @Indexed
    private String customerId;

    @Indexed
    private String orderId;

    @Indexed(unique = true)
    private String sourceEventId;

    private NotificationType type;

    private NotificationChannel channel = NotificationChannel.IN_APP;

    private NotificationStatus status = NotificationStatus.UNREAD;

    private String title;

    private String message;

    private String correlationId;

    private Instant readAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated notification ID.
     *
     * @return notification ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated notification ID.
     *
     * @param id notification ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the customer that should see this notification.
     *
     * @return customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer that should see this notification.
     *
     * @param customerId customer ID
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the related order ID.
     *
     * @return order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the related order ID.
     *
     * @param orderId order ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the Kafka event ID that created this notification.
     *
     * @return source event ID
     */
    public String getSourceEventId() {
        return sourceEventId;
    }

    /**
     * Sets the Kafka event ID that created this notification.
     *
     * @param sourceEventId source event ID
     */
    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    /**
     * Returns the notification type.
     *
     * @return notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets the notification type.
     *
     * @param type notification type
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * Returns the notification channel.
     *
     * @return notification channel
     */
    public NotificationChannel getChannel() {
        return channel;
    }

    /**
     * Sets the notification channel.
     *
     * @param channel notification channel
     */
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    /**
     * Returns the read status.
     *
     * @return notification status
     */
    public NotificationStatus getStatus() {
        return status;
    }

    /**
     * Sets the read status.
     *
     * @param status notification status
     */
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    /**
     * Returns the notification title.
     *
     * @return title text
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the notification title.
     *
     * @param title title text
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the notification body.
     *
     * @return body text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification body.
     *
     * @param message body text
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the correlation ID that connects logs and events.
     *
     * @return correlation ID
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlation ID that connects logs and events.
     *
     * @param correlationId correlation ID
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Returns when the notification was read.
     *
     * @return read timestamp
     */
    public Instant getReadAt() {
        return readAt;
    }

    /**
     * Sets when the notification was read.
     *
     * @param readAt read timestamp
     */
    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    /**
     * Returns the optimistic locking version.
     *
     * @return document version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the optimistic locking version.
     *
     * @param version document version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Returns when the notification was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the notification was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the notification was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the notification was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
