package com.example.ems.onboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.onboarding.entity.OnboardingDocument;

import java.time.LocalDateTime;

public class OnboardingDocumentResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "string")
    private String fileType;
    @Schema(example = "string")
    private String downloadUrl;
    @Schema(example = "string")
    private String documentType;
    @Schema(example = "ACTIVE")
    private String verificationStatus;
    @Schema(example = "string")
    private String verificationNotes;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime uploadedAt;

    public OnboardingDocumentResponse() {}

    public OnboardingDocumentResponse(OnboardingDocument doc) {
        this.id = doc.getId();
        this.fileName = doc.getFileName();
        this.fileType = doc.getFileType();
        this.downloadUrl = doc.getDownloadUrl();
        this.documentType = doc.getDocumentType();
        this.verificationStatus = doc.getVerificationStatus();
        this.verificationNotes = doc.getVerificationNotes();
        this.uploadedAt = doc.getUploadedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
}
