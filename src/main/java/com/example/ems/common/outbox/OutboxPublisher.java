package com.example.ems.common.outbox;

import com.example.ems.common.kafka.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Fetches pending outbox events and publishes them to Kafka.
 * Uses native FOR UPDATE SKIP LOCKED via the repository for concurrent safety.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxEventRepository;
    private final EventPublisher eventPublisher;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
            EventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Fetch a batch of pending events and publish them to Kafka.
     * Must run within a transaction to hold the row locks.
     */
    @Transactional
    public int publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .fetchUnprocessedBatchForUpdate(BATCH_SIZE);

        if (events.isEmpty()) {
            return 0;
        }

        log.info("Processing {} outbox events", events.size());
        int published = 0;

        for (OutboxEvent event : events) {
            try {
                event.setStatus(OutboxStatus.PROCESSING);

                String partitionKey = event.getPartitionKey() != null
                        ? event.getPartitionKey()
                        : event.getAggregateId();

                eventPublisher.publish(
                        event.getEventType(),
                        partitionKey,
                        event.getPayload()).get(); // Block to ensure delivery before marking published

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                published++;

                log.debug("Published outbox event: id={}, type={}",
                        event.getId(), event.getEventType());

            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());

                if (event.getRetryCount() >= event.getMaxRetries()) {
                    event.setStatus(OutboxStatus.FAILED_PERMANENT);
                    log.error("Outbox event permanently failed: id={}, type={}, retries={}",
                            event.getId(), event.getEventType(), event.getRetryCount(), e);
                } else {
                    event.setStatus(OutboxStatus.FAILED);
                    log.warn("Outbox event failed (retry {}/{}): id={}, type={}",
                            event.getRetryCount(), event.getMaxRetries(),
                            event.getId(), event.getEventType(), e);
                }
            }
        }

        outboxEventRepository.saveAll(events);
        log.info("Outbox batch complete: {}/{} published", published, events.size());
        return published;
    }
}
