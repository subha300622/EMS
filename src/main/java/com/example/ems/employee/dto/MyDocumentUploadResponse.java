package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class MyDocumentUploadResponse {

    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "string")
    private String documentType;
    @Schema(example = "1")
    private int version;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "ACTIVE")
    private String verificationStatus;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime uploadedAt;

    public MyDocumentUploadResponse() {}

    public MyDocumentUploadResponse(Long documentId, String fileName, String documentType, int version, String status, String verificationStatus, LocalDateTime uploadedAt) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.documentType = documentType;
        this.version = version;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
