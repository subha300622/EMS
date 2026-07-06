package com.example.ems.reports.revenue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRevenueExportFormatException extends RevenueReportException {
    public InvalidRevenueExportFormatException(String message) {
        super(message);
    }
}
