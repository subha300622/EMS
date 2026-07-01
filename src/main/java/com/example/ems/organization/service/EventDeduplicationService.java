package com.example.ems.organization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Service managing stateful database-level idempotency locks to guarantee exactly-once execution safety.
 */
@Service
public class EventDeduplicationService {

    private static final Logger log = LoggerFactory.getLogger(EventDeduplicationService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Claims the event in a separate database transaction context.
     * Returns true if claiming succeeds (status set to PROCESSING or transition allowed); returns false if it is already completed.
     * Throws IdempotentConflictException if a concurrent execution is already in progress, hash mismatch occurs, or event is permanently failed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claimEvent(String eventId, String handlerName, String requestHash) {
        String eventKey = eventId + ":" + handlerName;
        java.sql.Timestamp now = java.sql.Timestamp.from(Instant.now());
        try {
            jdbcTemplate.update(
                "INSERT INTO idempotency_keys (idempotency_key, status, created_at, updated_at, request_hash) VALUES (?, 'PROCESSING', ?, ?, ?)",
                eventKey, now, now, requestHash
            );
            log.info("[EventDeduplicationService] Successfully created processing idempotency lock for: {}", eventKey);
            return true;
        } catch (Exception e) {
            log.info("[EventDeduplicationService] Idempotency key already exists for: {}. Attempting stateful transition...", eventKey);
            
            try {
                // Concurrency-safe stateful transition using atomic UPDATE row lock
                int updated = jdbcTemplate.update(
                    "UPDATE idempotency_keys SET status = 'PROCESSING', updated_at = ? WHERE idempotency_key = ? AND status = 'FAILED_TRANSIENT'",
                    now, eventKey
                );
                if (updated == 1) {
                    log.info("[EventDeduplicationService] Re-acquired processing lock from FAILED_TRANSIENT for: {}", eventKey);
                    return true;
                }

                // If no row was updated, query status to determine conflict details
                Map<String, Object> keyDetails = jdbcTemplate.queryForMap(
                    "SELECT status, request_hash FROM idempotency_keys WHERE idempotency_key = ?",
                    eventKey
                );
                
                String status = (String) keyDetails.get("status");
                String dbHash = (String) keyDetails.get("request_hash");
                
                if (requestHash != null && dbHash != null && !requestHash.equals(dbHash)) {
                    throw new com.example.ems.subscription.exception.IdempotentConflictException(
                        "Idempotency request hash mismatch for key: " + eventKey
                    );
                }
                
                if ("COMPLETED".equals(status)) {
                    log.info("[EventDeduplicationService] Event {} already COMPLETED. Skipping execution.", eventKey);
                    return false;
                } else if ("PROCESSING".equals(status)) {
                    log.warn("[EventDeduplicationService] Event {} currently PROCESSING by another thread.", eventKey);
                    throw new com.example.ems.subscription.exception.IdempotentConflictException(
                        "Concurrent processing conflict for event: " + eventKey
                    );
                } else if ("FAILED_PERMANENT".equals(status)) {
                    log.error("[EventDeduplicationService] Event {} has failed permanently. Blocking execution.", eventKey);
                    throw new com.example.ems.subscription.exception.IdempotentConflictException(
                        "Execution blocked: Event has failed permanently: " + eventKey
                    );
                }
            } catch (com.example.ems.subscription.exception.IdempotentConflictException ice) {
                throw ice;
            } catch (Exception ex) {
                log.error("[EventDeduplicationService] Error resolving claim collision for key " + eventKey, ex);
                return false;
            }
            return false;
        }
    }

    /**
     * Marks the idempotency key lock status as COMPLETED.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeKey(String eventId, String handlerName) {
        String eventKey = eventId + ":" + handlerName;
        java.sql.Timestamp now = java.sql.Timestamp.from(Instant.now());
        jdbcTemplate.update(
            "UPDATE idempotency_keys SET status = 'COMPLETED', updated_at = ? WHERE idempotency_key = ?",
            now, eventKey
        );
        log.info("[EventDeduplicationService] Marked idempotency lock as COMPLETED: {}", eventKey);
    }

    /**
     * Marks the idempotency key lock status as FAILED_TRANSIENT or FAILED_PERMANENT.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failKeyWithStatus(String eventId, String handlerName, String failureStatus) {
        String eventKey = eventId + ":" + handlerName;
        java.sql.Timestamp now = java.sql.Timestamp.from(Instant.now());
        jdbcTemplate.update(
            "UPDATE idempotency_keys SET status = ?, updated_at = ? WHERE idempotency_key = ?",
            failureStatus, now, eventKey
        );
        log.info("[EventDeduplicationService] Marked idempotency lock as {}: {}", failureStatus, eventKey);
    }
}
