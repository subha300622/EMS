package com.example.ems.common.event;

/**
 * Interface for publishing domain events.
 * Business services use this to emit events within their @Transactional boundaries.
 * The implementation persists events to the outbox table, keeping business logic
 * completely decoupled from Kafka or any messaging infrastructure.
 */
public interface DomainEventPublisher {

    /**
     * Publish a domain event by persisting it to the outbox table.
     * Must be called within an active @Transactional context.
     *
     * @param envelope the event envelope containing metadata and payload
     */
    void publish(EventEnvelope<?> envelope);
}
