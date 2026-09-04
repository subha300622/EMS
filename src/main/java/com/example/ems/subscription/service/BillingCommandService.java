package com.example.ems.subscription.service;

import com.example.ems.organization.service.EventDeduplicationService;
import com.example.ems.subscription.exception.IdempotentConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Orchestrator acting as the single gatekeeper entry point for all execution flows (initial handlers and retries).
 */
@Service
public class BillingCommandService {

    private static final Logger log = LoggerFactory.getLogger(BillingCommandService.class);

    @Autowired
    private EventDeduplicationService deduplicationService;

    @Autowired
    private BillingDomainService billingDomainService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Executes payment processing. Coordinates idempotency claims, atomic domain updates, and performance logging.
     */
    public void processPaymentSuccess(String eventId, String handlerName, Long invoiceId, Long subscriptionId, String gatewayPaymentId) {
        log.info("[BillingCommandService] processPaymentSuccess gate entered for event: {}", eventId);

        String requestHash = invoiceId + ":" + subscriptionId + ":" + gatewayPaymentId;
        boolean claimed;

        // Step 1: Idempotency Claim Check
        try {
            claimed = deduplicationService.claimEvent(eventId, handlerName, requestHash);
        } catch (IdempotentConflictException e) {
            log.warn("[BillingCommandService] Idempotency conflict for event: {}. Details: {}", eventId, e.getMessage());
            throw e;
        }

        if (!claimed) {
            log.info("[BillingCommandService] Duplicate execution detected for event: {}:{}. Already processed. Skipping.", eventId, handlerName);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            // Step 2: Domain Execution
            billingDomainService.processPaymentSuccess(invoiceId, subscriptionId, gatewayPaymentId);

            long duration = System.currentTimeMillis() - start;
            log.info("[BillingCommandService] Domain execution succeeded for event: {} in {}ms", eventId, duration);

            // Step 3: Log SUCCESS status & Mark Completed
            deduplicationService.completeKey(eventId, handlerName);
            logExecution(eventId, handlerName, "SUCCESS", null, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("[BillingCommandService] Domain execution failed for event: {} in {}ms: {}", eventId, duration, e.getMessage(), e);

            // Step 3: Classify transient vs permanent failure
            boolean isTransient = isTransientFailure(e);
            String failureStatus = isTransient ? "FAILED_TRANSIENT" : "FAILED_PERMANENT";

            // Mark lock as FAILED_TRANSIENT or FAILED_PERMANENT
            deduplicationService.failKeyWithStatus(eventId, handlerName, failureStatus);
            logExecution(eventId, handlerName, failureStatus, e.getMessage(), duration);

            // Re-throw to propagate transaction rollback
            throw e;
        }
    }

    private boolean isTransientFailure(Exception e) {
        if (e instanceof com.example.ems.subscription.exception.InvalidStateTransitionException) {
            return false; // Permanent FSM transition failure
        }
        if (e instanceof IllegalArgumentException) {
            return false; // Permanent validation failure
        }
        
        // Check for common transient/database locking/deadlock signatures
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("deadlock") || msg.contains("lock timeout") || msg.contains("optimistic lock") || msg.contains("pessimistic lock")) {
            return true;
        }
        
        // Treat database connection or query timeouts as transient
        if (e instanceof org.springframework.dao.TransientDataAccessException || e instanceof org.springframework.transaction.TransactionSystemException) {
            return true;
        }
        
        // Fallback to transient for network/gateway retries unless known to be permanent
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExecution(String eventId, String handlerName, String status, String errorMsg, long durationMs) {
        try {
            jdbcTemplate.update(
                "INSERT INTO event_execution_log (event_id, handler_name, status, error_message, duration_ms, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                eventId, handlerName, status, errorMsg, durationMs, java.sql.Timestamp.from(Instant.now())
            );
            log.info("[BillingCommandService] Wrote execution audit log for event: {}", eventId);
        } catch (Exception ex) {
            log.error("[BillingCommandService] Failed to write audit log for event {}: {}", eventId, ex.getMessage(), ex);
        }
    }
}
