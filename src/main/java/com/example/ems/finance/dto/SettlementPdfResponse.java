package com.example.ems.finance.dto;

public class SettlementPdfResponse {
    private String fileName;
    private String downloadUrl;

    public SettlementPdfResponse() {}

    public SettlementPdfResponse(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
