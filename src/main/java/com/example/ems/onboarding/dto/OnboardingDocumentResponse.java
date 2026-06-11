package com.example.ems.onboarding.dto;

import com.example.ems.onboarding.entity.OnboardingDocument;

import java.time.LocalDateTime;

public class OnboardingDocumentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private String verificationStatus;
    private String verificationNotes;
    private LocalDateTime uploadedAt;

    public OnboardingDocumentResponse() {}

    public OnboardingDocumentResponse(OnboardingDocument doc) {
        this.id = doc.getId();
        this.fileName = doc.getFileName();
        this.fileType = doc.getFileType();
        this.downloadUrl = doc.getDownloadUrl();
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
}
