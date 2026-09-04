package com.example.ems.reports.organization.dto;

public class ExportHistoryResponse {
    private Long id;
    private String reportName;
    private String createdBy;
    private String exportType;
    private String exportFormat;
    private String status;
    private String downloadUrl;
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
