package com.example.ems.reports.subscription.dto;

import java.time.LocalDateTime;

public class SubscriptionExportMetadata {
    private String fileName;
    private String format;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private int recordCount;

    public SubscriptionExportMetadata() {}

    public SubscriptionExportMetadata(String fileName, String format, String generatedBy, LocalDateTime generatedAt, int recordCount) {
        this.fileName = fileName;
        this.format = format;
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
        this.recordCount = recordCount;
    }

    // Getters and Setters
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
