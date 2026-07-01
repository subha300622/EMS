package com.example.ems.subscription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Scheduled worker that claims and re-executes pending items in the event retry queue.
 */
@Component
public class BillingRetryWorker {

    private static final Logger log = LoggerFactory.getLogger(BillingRetryWorker.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BillingCommandService billingCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Polls the DLQ for PENDING retry records. Concurrency is safe due to FOR UPDATE SKIP LOCKED.
     */
    @Scheduled(fixedDelay = 60000)
    public void processRetries() {
        log.info("[BillingRetryWorker] Polling for pending retries...");
        try {
            jdbcTemplate.query(
                "SELECT id, event_id, event_type, handler_name, payload, retry_count, max_retries FROM event_retry_queue " +
                "WHERE status = 'PENDING' AND next_retry_at <= ? " +
                "ORDER BY next_retry_at ASC, retry_count ASC " +
                "FOR UPDATE SKIP LOCKED",
                rs -> {
                    long id = rs.getLong("id");
                    String eventId = rs.getString("event_id");
                    String eventType = rs.getString("event_type");
                    String handlerName = rs.getString("handler_name");
                    String payload = rs.getString("payload");
                    int retryCount = rs.getInt("retry_count");
                    int maxRetries = rs.getInt("max_retries");

                    log.info("[BillingRetryWorker] Processing retry ID: {}, Event: {}, Handler: {}, Count: {}", id, eventId, handlerName, retryCount);

                    // Mark as RETRYING immediately to prevent concurrent worker picking
                    jdbcTemplate.update("UPDATE event_retry_queue SET status = 'RETRYING', updated_at = ? WHERE id = ?", java.sql.Timestamp.from(Instant.now()), id);

                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = objectMapper.readValue(payload, Map.class);
                        Long invoiceId = ((Number) map.get("invoiceId")).longValue();
                        Long subscriptionId = ((Number) map.get("subscriptionId")).longValue();
                        String gatewayPaymentId = (String) map.get("gatewayPaymentId");

                        // Re-execute through the same command gate
                        billingCommandService.processPaymentSuccess(eventId, handlerName, invoiceId, subscriptionId, gatewayPaymentId);

                        // Success -> Delete retry queue row
                        jdbcTemplate.update("DELETE FROM event_retry_queue WHERE id = ?", id);
                        log.info("[BillingRetryWorker] Retry ID: {} completed successfully. Deleted from queue.", id);
                    } catch (Exception e) {
                        log.error("[BillingRetryWorker] Retry ID: {} failed: {}", id, e.getMessage());
                        handleFailure(id, retryCount, maxRetries, e.getMessage());
                    }
                },
                java.sql.Timestamp.from(Instant.now())
            );
        } catch (Exception e) {
            log.error("[BillingRetryWorker] Error polling retries: {}", e.getMessage(), e);
        }
    }

    private void handleFailure(long id, int retryCount, int maxRetries, String errorMsg) {
        try {
            int nextCount = retryCount + 1;
            Instant now = Instant.now();

            if (nextCount >= maxRetries) {
                // Terminate as DEAD (Dead Letter State)
                jdbcTemplate.update(
                    "UPDATE event_retry_queue SET status = 'DEAD', retry_count = ?, last_error = ?, dead_letter_reason = ?, dead_letter_timestamp = ?, updated_at = ? WHERE id = ?",
                    nextCount, errorMsg, "Max retries exceeded", java.sql.Timestamp.from(now), java.sql.Timestamp.from(now), id
                );
                log.warn("[BillingRetryWorker] Retry ID: {} has been marked DEAD (max retries reached).", id);
            } else {
                // Compute capped exponential backoff delay: min(30, 2^retryCount) minutes
                long backoffMinutes = (long) Math.min(30, Math.pow(2, nextCount));
                Instant nextRun = now.plusSeconds(backoffMinutes * 60);

                jdbcTemplate.update(
                    "UPDATE event_retry_queue SET status = 'PENDING', retry_count = ?, last_error = ?, next_retry_at = ?, updated_at = ? WHERE id = ?",
                    nextCount, errorMsg, java.sql.Timestamp.from(nextRun), java.sql.Timestamp.from(now), id
                );
                log.info("[BillingRetryWorker] Scheduled next retry for ID: {} at {}", id, nextRun);
            }
        } catch (Exception ex) {
            log.error("[BillingRetryWorker] Failed to update failure state for ID {}: {}", id, ex.getMessage(), ex);
        }
    }
}
