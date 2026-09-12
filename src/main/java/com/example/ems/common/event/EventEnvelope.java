package com.example.ems.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard envelope wrapper for all domain events.
 * Contains metadata for tracing, versioning, and routing.
 */
public class EventEnvelope<T> {

    private UUID eventId;
    private String eventType;
    private String eventVersion;
    private String aggregateType;
    private String aggregateId;
    private String correlationId;
    private String causationId;
    private String partitionKey;
    private Instant timestamp;
    private T payload;

    public EventEnvelope() {
    }

    public EventEnvelope(String eventType, String aggregateType, String aggregateId, T payload) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.eventVersion = "1.0";
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.correlationId = UUID.randomUUID().toString();
        this.partitionKey = aggregateId;
        this.timestamp = Instant.now();
        this.payload = payload;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}
