package com.example.ems.reports.subscription.validator;

import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.reports.subscription.dto.SubscriptionExportRequest;
import com.example.ems.reports.subscription.dto.SubscriptionReportFilterRequest;
import com.example.ems.reports.subscription.exception.InvalidExportFormatException;
import com.example.ems.reports.subscription.exception.InvalidSubscriptionReportFilterException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionReportValidator {

    private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "organizationId", "organizationName", "plan", "status", "billingCycle", 
            "subscriptionStart", "subscriptionEnd", "amount"
    ));

    private static final Set<String> ALLOWED_FORMATS = new HashSet<>(Arrays.asList("CSV", "EXCEL", "PDF"));
    private static final Set<String> ALLOWED_BILLING_CYCLES = new HashSet<>(Arrays.asList("MONTHLY", "YEARLY"));

    public void validateFilter(SubscriptionReportFilterRequest request) {
        if (request == null) {
            throw new InvalidSubscriptionReportFilterException("Request cannot be null");
        }

        // Date range
        if (request.getFromDate() != null && request.getToDate() != null) {
            if (request.getFromDate().isAfter(request.getToDate())) {
                throw new InvalidSubscriptionReportFilterException("From date cannot be after to date");
            }
        }

        // Page size
        if (request.getSize() != null && (request.getSize() <= 0 || request.getSize() > 100)) {
            throw new InvalidSubscriptionReportFilterException("Page size must be between 1 and 100");
        }

        // Sort fields
        if (request.getSortBy() != null && !ALLOWED_SORT_FIELDS.contains(request.getSortBy())) {
            throw new InvalidSubscriptionReportFilterException("Invalid sort field: " + request.getSortBy());
        }

        // Billing Cycle
        if (request.getBillingCycle() != null && !request.getBillingCycle().isEmpty()) {
            if (!ALLOWED_BILLING_CYCLES.contains(request.getBillingCycle().toUpperCase())) {
                throw new InvalidSubscriptionReportFilterException("Invalid billing cycle: " + request.getBillingCycle());
            }
        }

        // Status
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                SubscriptionStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidSubscriptionReportFilterException("Invalid subscription status: " + request.getStatus());
            }
        }
    }

    public void validateExport(SubscriptionExportRequest request) {
        if (request == null) {
            throw new InvalidExportFormatException("Export request cannot be null");
        }

        if (request.getFormat() == null || !ALLOWED_FORMATS.contains(request.getFormat().toUpperCase())) {
            throw new InvalidExportFormatException("Unsupported export format: " + request.getFormat());
        }

        // Validate dates
        if (request.getFromDate() != null && request.getToDate() != null) {
            if (request.getFromDate().isAfter(request.getToDate())) {
                throw new InvalidSubscriptionReportFilterException("From date cannot be after to date");
            }
        }

        // Sort fields
        if (request.getSortBy() != null && !ALLOWED_SORT_FIELDS.contains(request.getSortBy())) {
            throw new InvalidSubscriptionReportFilterException("Invalid sort field: " + request.getSortBy());
        }
    }
}
