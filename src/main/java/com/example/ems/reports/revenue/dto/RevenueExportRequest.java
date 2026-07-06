package com.example.ems.reports.revenue.dto;

public class RevenueExportRequest extends RevenueFilterRequest {
    private String format;
    private String type;

    public RevenueExportRequest() {}

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
