package com.eventcart.order.outbox;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document that stores an event waiting to be published to Kafka.
 */
@Document(collection = "outbox_events")
public class OrderOutboxEventDocument {
    @Id
    private String id;

    @Indexed
    private String aggregateType;

    @Indexed
    private String aggregateId;

    @Indexed
    private String eventType;

    private String topic;

    private String eventKey;

    private String payloadJson;

    @Indexed
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    private int publishAttempts;

    private String lastError;

    private Instant publishedAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated outbox ID.
     *
     * @return outbox event ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated outbox ID.
     *
     * @param id outbox event ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the aggregate type that produced the event.
     *
     * @return aggregate type
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Sets the aggregate type that produced the event.
     *
     * @param aggregateType aggregate type
     */
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    /**
     * Returns the aggregate ID related to the event.
     *
     * @return aggregate ID
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Sets the aggregate ID related to the event.
     *
     * @param aggregateId aggregate ID
     */
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    /**
     * Returns the domain event type.
     *
     * @return event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the domain event type.
     *
     * @param eventType event type
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the Kafka topic for the event.
     *
     * @return topic name
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the Kafka topic for the event.
     *
     * @param topic topic name
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Returns the Kafka message key.
     *
     * @return event key
     */
    public String getEventKey() {
        return eventKey;
    }

    /**
     * Sets the Kafka message key.
     *
     * @param eventKey event key
     */
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    /**
     * Returns the serialized event payload.
     *
     * @return event JSON
     */
    public String getPayloadJson() {
        return payloadJson;
    }

    /**
     * Sets the serialized event payload.
     *
     * @param payloadJson event JSON
     */
    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    /**
     * Returns the publication status.
     *
     * @return outbox event status
     */
    public OutboxEventStatus getStatus() {
        return status;
    }

    /**
     * Sets the publication status.
     *
     * @param status outbox event status
     */
    public void setStatus(OutboxEventStatus status) {
        this.status = status;
    }

    /**
     * Returns how many publish attempts have been made.
     *
     * @return publish attempt count
     */
    public int getPublishAttempts() {
        return publishAttempts;
    }

    /**
     * Sets how many publish attempts have been made.
     *
     * @param publishAttempts publish attempt count
     */
    public void setPublishAttempts(int publishAttempts) {
        this.publishAttempts = publishAttempts;
    }

    /**
     * Returns the last publish failure message.
     *
     * @return last error message
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Sets the last publish failure message.
     *
     * @param lastError last error message
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Returns when the event was published.
     *
     * @return publish timestamp
     */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /**
     * Sets when the event was published.
     *
     * @param publishedAt publish timestamp
     */
    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
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
     * Returns when the outbox event was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the outbox event was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the outbox event was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the outbox event was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
