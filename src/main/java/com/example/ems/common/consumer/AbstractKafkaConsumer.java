package com.example.ems.common.consumer;

import com.example.ems.common.event.EventEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Abstract base class for Kafka consumers.
 * Handles deserialization, idempotency checking, and processed event tracking.
 * Subclasses only need to implement handleEvent() with their domain-specific logic.
 */
public abstract class AbstractKafkaConsumer {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    protected AbstractKafkaConsumer(ProcessedEventRepository processedEventRepository,
                                    ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * @return the consumer group name for idempotency tracking
     */
    protected abstract String getConsumerGroup();

    /**
     * Process the deserialized event envelope.
     */
    protected abstract void handleEvent(EventEnvelope<Object> envelope);

    /**
     * Entry point for consuming messages. Handles deserialization,
     * idempotency checking, processing, and recording.
     */
    protected void consume(String message) {
        EventEnvelope<Object> envelope;
        try {
            envelope = objectMapper.readValue(message,
                    new TypeReference<EventEnvelope<Object>>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize event message: {}", e.getMessage(), e);
            return;
        }

        UUID eventId = envelope.getEventId();
        String consumerGroup = getConsumerGroup();

        // Idempotency check
        if (processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup)) {
            log.info("Event already processed, skipping: eventId={}, consumer={}",
                    eventId, consumerGroup);
            return;
        }

        log.info("Processing event: type={}, eventId={}, consumer={}",
                envelope.getEventType(), eventId, consumerGroup);

        try {
            handleEvent(envelope);

            // Record as processed
            processedEventRepository.save(new ProcessedEvent(eventId, consumerGroup));
            log.info("Event processed successfully: eventId={}, consumer={}",
                    eventId, consumerGroup);

        } catch (Exception e) {
            log.error("Failed to process event: eventId={}, consumer={}, error={}",
                    eventId, consumerGroup, e.getMessage(), e);
            throw e; // Let Kafka retry mechanism handle it
        }
    }
}
