package com.example.ems.reports.revenue.exception;

public class RevenueReportException extends RuntimeException {
    public RevenueReportException(String message) {
        super(message);
    }

    public RevenueReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
