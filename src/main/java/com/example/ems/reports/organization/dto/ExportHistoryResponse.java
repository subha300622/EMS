package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Report export history log entry response")
public class ExportHistoryResponse {
    @Schema(description = "Export History ID", example = "1")
    private Long id;

    @Schema(description = "Report Name", example = "Organizations-2026")
    private String reportName;

    @Schema(description = "Created By User Email", example = "admin@example.com")
    private String createdBy;

    @Schema(description = "Export Category / Type", example = "ORGANIZATION")
    private String exportType;

    @Schema(description = "File Format", example = "CSV")
    private String exportFormat;

    @Schema(description = "Export Job Status", example = "COMPLETED")
    private String status;

    @Schema(description = "File Download URL", example = "/api/v1/platform/reports/organizations/export/download/1")
    private String downloadUrl;

    @Schema(description = "Created Date/Time", example = "2026-07-03T10:15:00Z")
    private String createdTime;

    public ExportHistoryResponse() {}

    public ExportHistoryResponse(Long id, String reportName, String createdBy, String exportType, String exportFormat, String status, String downloadUrl, String createdTime) {
        this.id = id;
        this.reportName = reportName;
        this.createdBy = createdBy;
        this.exportType = exportType;
        this.exportFormat = exportFormat;
        this.status = status;
        this.downloadUrl = downloadUrl;
        this.createdTime = createdTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getExportType() { return exportType; }
    public void setExportType(String exportType) { this.exportType = exportType; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }
}
