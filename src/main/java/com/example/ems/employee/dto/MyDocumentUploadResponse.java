package com.example.ems.employee.dto;

import java.time.LocalDateTime;

public class MyDocumentUploadResponse {

    private Long documentId;
    private String fileName;
    private String documentType;
    private int version;
    private String status;
    private String verificationStatus;
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
