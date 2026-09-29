package com.example.ems.reports.revenue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRevenueReportFilterException extends RevenueReportException {
    public InvalidRevenueReportFilterException(String message) {
        super(message);
    }
}
