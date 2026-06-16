package com.example.ems.employee.dto;

import java.time.LocalDateTime;

public class MyDocumentReplaceResponse {

    private Long documentId;
    private int previousVersion;
    private int newVersion;
    private String status;
    private LocalDateTime updatedAt;

    public MyDocumentReplaceResponse() {}

    public MyDocumentReplaceResponse(Long documentId, int previousVersion, int newVersion, String status, LocalDateTime updatedAt) {
        this.documentId = documentId;
        this.previousVersion = previousVersion;
        this.newVersion = newVersion;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public int getPreviousVersion() { return previousVersion; }
    public void setPreviousVersion(int previousVersion) { this.previousVersion = previousVersion; }
    public int getNewVersion() { return newVersion; }
    public void setNewVersion(int newVersion) { this.newVersion = newVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
