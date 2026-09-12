package com.example.ems.organization.event;

/**
 * Minimal and immutable event representing a successful payment checkout session.
 * Fired to trigger downstream updates of invoice status, subscription activation, history logging, and notifications.
 */
public record PaymentSucceededEvent(
    Long paymentId,
    Long invoiceId,
    Long subscriptionId,
    String gatewayPaymentId,
    String eventId
) {}
