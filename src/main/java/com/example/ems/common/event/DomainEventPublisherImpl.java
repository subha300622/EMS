package com.example.ems.common.event;

import com.example.ems.common.outbox.OutboxEvent;
import com.example.ems.common.outbox.OutboxEventRepository;
import com.example.ems.common.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of DomainEventPublisher that persists events to the outbox table
 * within the caller's active @Transactional context. This guarantees atomicity
 * between the business operation and the event publication.
 */
@Component
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherImpl.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public DomainEventPublisherImpl(OutboxEventRepository outboxEventRepository,
                                     ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(EventEnvelope<?> envelope) {
        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setId(envelope.getEventId());
            outboxEvent.setAggregateType(envelope.getAggregateType());
            outboxEvent.setAggregateId(envelope.getAggregateId());
            outboxEvent.setEventType(envelope.getEventType());
            outboxEvent.setPayload(objectMapper.writeValueAsString(envelope));
            outboxEvent.setStatus(OutboxStatus.PENDING);
            outboxEvent.setEventVersion(envelope.getEventVersion());
            outboxEvent.setCorrelationId(envelope.getCorrelationId());
            outboxEvent.setCausationId(envelope.getCausationId());
            outboxEvent.setPartitionKey(envelope.getPartitionKey());

            outboxEventRepository.save(outboxEvent);

            log.info("Domain event published to outbox: type={}, id={}, aggregate={}:{}",
                    envelope.getEventType(), envelope.getEventId(),
                    envelope.getAggregateType(), envelope.getAggregateId());

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize event payload for type: " + envelope.getEventType(), e);
        }
    }
}
