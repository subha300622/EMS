package com.example.ems.reports.revenue.validator;

import com.example.ems.reports.revenue.dto.RevenueExportRequest;
import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import com.example.ems.reports.revenue.exception.InvalidRevenueExportFormatException;
import com.example.ems.reports.revenue.exception.InvalidRevenueReportFilterException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Component
public class RevenueReportValidator {

    private static final List<String> VALID_SORT_FIELDS = Arrays.asList(
        "id", "amount", "paidAt", "createdAt", "status", "currency", "gateway", "organizationId", "invoiceNumber"
    );

    public void validateFilter(RevenueFilterRequest request) {
        if (request == null) {
            throw new InvalidRevenueReportFilterException("Request cannot be null");
        }

        if (request.getFrom() != null && request.getTo() != null) {
            try {
                LocalDate fromDate = LocalDate.parse(request.getFrom());
                LocalDate toDate = LocalDate.parse(request.getTo());
                if (fromDate.isAfter(toDate)) {
                    throw new InvalidRevenueReportFilterException("Start date 'from' cannot be after end date 'to'");
                }
            } catch (DateTimeParseException e) {
                throw new InvalidRevenueReportFilterException("Dates must be in format YYYY-MM-DD");
            }
        }

        if (request.getMinAmount() != null && request.getMaxAmount() != null) {
            if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
                throw new InvalidRevenueReportFilterException("Minimum amount cannot be greater than maximum amount");
            }
        }

        if (request.getSortBy() != null && !VALID_SORT_FIELDS.contains(request.getSortBy())) {
            throw new InvalidRevenueReportFilterException("Invalid sort field: " + request.getSortBy());
        }

        if (request.getDirection() != null &&
            !request.getDirection().equalsIgnoreCase("asc") &&
            !request.getDirection().equalsIgnoreCase("desc")) {
            throw new InvalidRevenueReportFilterException("Invalid sort direction: " + request.getDirection());
        }
    }

    public void validateHorizon(int horizon) {
        if (horizon != 3 && horizon != 6 && horizon != 12) {
            throw new InvalidRevenueReportFilterException("Forecast horizon must be either 3, 6, or 12 months");
        }
    }

    public void validateExport(RevenueExportRequest request) {
        validateFilter(request);

        if (request.getType() == null) {
            throw new InvalidRevenueExportFormatException("Export type must be provided");
        }

        List<String> validTypes = Arrays.asList("PAYMENTS", "INVOICES", "REFUNDS", "PLAN_REVENUE", "FORECAST", "SUMMARY");
        if (!validTypes.contains(request.getType().toUpperCase())) {
            throw new InvalidRevenueExportFormatException("Invalid export type: " + request.getType());
        }

        if (request.getFormat() == null) {
            throw new InvalidRevenueExportFormatException("Export format must be provided");
        }

        String format = request.getFormat().toUpperCase();
        if (!"CSV".equals(format) && !"EXCEL".equals(format) && !"PDF".equals(format)) {
            throw new InvalidRevenueExportFormatException("Invalid export format: " + request.getFormat());
        }
    }
}
