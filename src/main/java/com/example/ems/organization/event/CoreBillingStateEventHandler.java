package com.example.ems.organization.event;

import com.example.ems.subscription.service.BillingCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener coordinating invoice payment and subscription activation within the parent database transaction context.
 */
@Component
public class CoreBillingStateEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CoreBillingStateEventHandler.class);

    @Autowired
    private BillingCommandService billingCommandService;

    /**
     * Listens for payment succeeded events synchronously. Runs in the same transaction as verifyPayment.
     */
    @EventListener
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        String handlerName = "CoreBillingStateEventHandler";
        log.info("[CoreBillingStateEventHandler] Core billing state event received: {}", event.eventId());

        // Execute unified core transitions through the single command gate
        billingCommandService.processPaymentSuccess(
            event.eventId(),
            handlerName,
            event.invoiceId(),
            event.subscriptionId(),
            event.gatewayPaymentId()
        );
    }
}
