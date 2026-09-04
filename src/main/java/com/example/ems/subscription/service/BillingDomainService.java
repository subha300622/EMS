package com.example.ems.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service encapsulating atomic business transactions for subscription billing updates.
 */
@Service
public class BillingDomainService {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Updates invoice to PAID and activates the subscription atomically inside a single transaction.
     */
    @Transactional
    public void processPaymentSuccess(Long invoiceId, Long subscriptionId, String gatewayPaymentId) {
        subscriptionService.markInvoiceAsPaid(invoiceId);
        subscriptionService.activateSubscription(subscriptionId, gatewayPaymentId);
    }
}
