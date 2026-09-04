package com.example.ems.reports.subscription.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSubscriptionReportFilterException extends SubscriptionReportException {
    public InvalidSubscriptionReportFilterException(String message) {
        super(message);
    }
}
