package com.example.ems.organization.event;

import com.example.ems.organization.service.EventRetryQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener logging failed payment events to the retry queue when the parent transaction rolls back.
 */
@Component
public class BillingRollbackEventHandler {

    private static final Logger log = LoggerFactory.getLogger(BillingRollbackEventHandler.class);

    @Autowired
    private EventRetryQueueService eventRetryQueueService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listens for transaction rollback. Logs the event payload to the retry queue for scheduled recovery.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleBillingRollback(PaymentSucceededEvent event) {
        String handlerName = "CoreBillingStateEventHandler";
        log.warn("[BillingRollbackEventHandler] Core transaction rolled back for event: {}. Logging to retry queue.", event.eventId());
        try {
            String payload = objectMapper.writeValueAsString(event);
            eventRetryQueueService.queueFailedEvent(
                event.eventId(),
                "PaymentSucceededEvent",
                handlerName,
                payload,
                "Transaction rolled back due to execution failure"
            );
        } catch (Exception e) {
            log.error("[BillingRollbackEventHandler] Failed to serialize event retry payload: {}", e.getMessage(), e);
        }
    }
}
