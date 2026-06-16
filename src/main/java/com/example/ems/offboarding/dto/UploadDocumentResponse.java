package com.example.ems.offboarding.dto;

import java.time.LocalDateTime;

public class UploadDocumentResponse {

    private Long documentId;
    private String documentName;
    private String documentType;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String status;

    public UploadDocumentResponse() {}

    public UploadDocumentResponse(Long documentId, String documentName, String documentType, String uploadedBy, LocalDateTime uploadedAt, String status) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentType = documentType;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.status = status;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
