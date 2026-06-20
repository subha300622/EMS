package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class MyDocumentReplaceResponse {

    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "1")
    private int previousVersion;
    @Schema(example = "1")
    private int newVersion;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
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
