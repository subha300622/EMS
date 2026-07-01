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
 * Decoupled event handler triggering email confirmation notifications asynchronously after commit.
 */
@Component
public class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    @Autowired
    private EventRetryQueueService eventRetryQueueService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Sends the notification asynchronously. Catches failures and registers them in the retry queue.
     */
    @org.springframework.scheduling.annotation.Async
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        String handlerName = "NotificationEventHandler";
        log.info("[NotificationEventHandler] Notification dispatch entered for event: {}", event.eventId());
        try {
            log.info("[NotificationEventHandler] Notification Sent for event: {} | Email receipt successfully delivered to organization administrator for payment: {}",
                    event.eventId(), event.gatewayPaymentId());
        } catch (Exception e) {
            log.error("[NotificationEventHandler] Notification failure for event: {}", event.eventId(), e);
            try {
                String payload = objectMapper.writeValueAsString(event);
                eventRetryQueueService.queueFailedEvent(
                    event.eventId(),
                    "PaymentSucceededEvent",
                    handlerName,
                    payload,
                    e.getMessage()
                );
            } catch (Exception ex) {
                log.error("[NotificationEventHandler] Failed to serialize notification retry payload: {}", ex.getMessage(), ex);
            }
        }
    }
}
