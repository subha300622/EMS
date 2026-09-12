package com.example.ems.organization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service managing dead-letter/failed event queue registration inside independent transactions.
 */
@Service
public class EventRetryQueueService {

    private static final Logger log = LoggerFactory.getLogger(EventRetryQueueService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Queues a failed event in the DLQ. Runs in an independent transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void queueFailedEvent(String eventId, String eventType, String handlerName, String payload, String errorMsg) {
        try {
            Instant now = Instant.now();
            Instant nextRetry = now.plusSeconds(60); // Initial delay: 60 seconds

            Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM event_retry_queue WHERE event_id = ? AND handler_name = ? AND status IN ('PENDING', 'RETRYING'))",
                Boolean.class, eventId, handlerName
            );

            if (Boolean.TRUE.equals(exists)) {
                jdbcTemplate.update(
                    "UPDATE event_retry_queue SET retry_count = retry_count + 1, status = 'PENDING', last_error = ?, next_retry_at = ?, updated_at = ? " +
                    "WHERE event_id = ? AND handler_name = ? AND status IN ('PENDING', 'RETRYING')",
                    errorMsg, java.sql.Timestamp.from(nextRetry), java.sql.Timestamp.from(now), eventId, handlerName
                );
                log.info("[EventRetryQueueService] Increment retry count for event {} in DLQ", eventId);
            } else {
                jdbcTemplate.update(
                    "INSERT INTO event_retry_queue (event_id, event_type, handler_name, payload, retry_count, max_retries, status, last_error, next_retry_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, 1, 3, 'PENDING', ?, ?, ?, ?)",
                    eventId, eventType, handlerName, payload, errorMsg, java.sql.Timestamp.from(nextRetry), java.sql.Timestamp.from(now), java.sql.Timestamp.from(now)
                );
                log.info("[EventRetryQueueService] Logged new failed event {} to DLQ", eventId);
            }
        } catch (Exception e) {
            log.error("[EventRetryQueueService] Failed to record DLQ for event {}: {}", eventId, e.getMessage(), e);
        }
    }
}
