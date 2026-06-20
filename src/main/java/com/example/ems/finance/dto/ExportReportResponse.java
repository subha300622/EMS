package com.example.ems.finance.dto;

import java.time.LocalDateTime;

public class ExportReportResponse {
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private LocalDateTime generatedAt;

    public ExportReportResponse() {}

    public ExportReportResponse(String fileName, String fileType, String downloadUrl, LocalDateTime generatedAt) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.generatedAt = generatedAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
