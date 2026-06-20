package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class UploadDocumentResponse {

    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "string")
    private String documentName;
    @Schema(example = "string")
    private String documentType;
    @Schema(example = "string")
    private String uploadedBy;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime uploadedAt;
    @Schema(example = "ACTIVE")
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
