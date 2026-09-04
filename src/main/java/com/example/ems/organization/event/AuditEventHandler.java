package com.example.ems.organization.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Decoupled event handler for system audit logging of gateway success confirmations.
 */
@Component
public class AuditEventHandler {

    private static final Logger log = LoggerFactory.getLogger(AuditEventHandler.class);

    @org.springframework.scheduling.annotation.Async
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[AuditEventHandler] Audited event: {} | PaymentId: {}, GatewayPaymentId: {}, InvoiceId: {}, SubscriptionId: {}",
                event.eventId(), event.paymentId(), event.gatewayPaymentId(), event.invoiceId(), event.subscriptionId());
    }
}
