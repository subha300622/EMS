package com.example.ems.reports.subscription.exception;

public class SubscriptionReportException extends RuntimeException {
    public SubscriptionReportException(String message) {
        super(message);
    }

    public SubscriptionReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
