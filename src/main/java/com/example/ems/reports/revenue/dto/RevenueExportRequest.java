package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request criteria for exporting revenue reports")
public class RevenueExportRequest extends RevenueFilterRequest {
    @Schema(description = "Export format (CSV, EXCEL, or PDF)", example = "CSV")
    private String format;

    @Schema(description = "Report Type (PAYMENTS, INVOICES, REFUNDS, or PLANS)", example = "PAYMENTS")
    private String type;

    public RevenueExportRequest() {}

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
