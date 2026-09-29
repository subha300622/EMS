package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for triggering export of organization report")
public record OrganizationReportExportRequest(
    @Schema(description = "Export format (CSV, EXCEL, or PDF)", example = "CSV")
    String format,

    @Schema(description = "Search query filter", example = "Acme")
    String search,

    @Schema(description = "Organization status filter", example = "ACTIVE")
    String status,

    @Schema(description = "Plan name filter", example = "ENTERPRISE")
    String plan
) {
    public String getEffectiveFormat() {
        return (format != null && !format.isBlank()) ? format.toUpperCase() : "CSV";
    }
}
