package com.example.ems.onboarding.dto;

import com.example.ems.onboarding.entity.OnboardingDocument;
import java.time.LocalDateTime;

public class OnboardingDocumentResponse {
    private String id;
    private String onboardingId;
    private String documentName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String status;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

    public OnboardingDocumentResponse() {}

    public OnboardingDocumentResponse(OnboardingDocument doc) {
        this.id = "doc-" + doc.getId();
        this.onboardingId = "onb-" + doc.getOnboarding().getId();
        this.documentName = doc.getDocumentType();
        this.fileName = doc.getFileName();
        this.fileType = doc.getFileType();
        this.fileSize = doc.getFileSize();
        this.fileUrl = doc.getDownloadUrl();
        this.status = doc.getVerificationStatus();
        this.uploadedBy = doc.getUploadedBy();
        this.uploadedAt = doc.getUploadedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOnboardingId() { return onboardingId; }
    public void setOnboardingId(String onboardingId) { this.onboardingId = onboardingId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
