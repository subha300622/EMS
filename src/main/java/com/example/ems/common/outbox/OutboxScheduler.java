package com.example.ems.common.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Scheduled executor that periodically triggers the OutboxPublisher.
 * Runs every 2 seconds to poll for pending events.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxPublisher outboxPublisher;

    public OutboxScheduler(OutboxPublisher outboxPublisher) {
        this.outboxPublisher = outboxPublisher;
    }

    @Scheduled(fixedDelayString = "${kafka.outbox.poll-interval-ms:2000}")
    public void pollOutbox() {
        try {
            int published = outboxPublisher.publishPendingEvents();
            if (published > 0) {
                log.debug("Outbox scheduler published {} events", published);
            }
        } catch (Exception e) {
            log.error("Outbox scheduler error: {}", e.getMessage(), e);
        }
    }
}
