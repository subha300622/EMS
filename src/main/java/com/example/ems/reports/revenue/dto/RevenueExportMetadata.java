package com.example.ems.reports.revenue.dto;

import java.time.LocalDateTime;

public class RevenueExportMetadata {
    private String fileName;
    private String format;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private int recordCount;

    public RevenueExportMetadata() {}

    public RevenueExportMetadata(String fileName, String format, String generatedBy, LocalDateTime generatedAt, int recordCount) {
        this.fileName = fileName;
        this.format = format;
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
        this.recordCount = recordCount;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }
}
