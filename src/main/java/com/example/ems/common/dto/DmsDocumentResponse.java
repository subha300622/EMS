package com.example.ems.common.dto;

import com.example.ems.common.entity.DmsDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DmsDocumentResponse {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private String status;
    private String category;
    private LocalDate expiryDate;
    private Long employeeId;
    private String employeeName;
    private String uploadedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DmsDocumentResponse() {}

    public DmsDocumentResponse(DmsDocument doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.description = doc.getDescription();
        this.fileName = doc.getFileName();
        this.fileType = doc.getFileType();
        this.fileSize = doc.getFileSize();
        this.downloadUrl = doc.getDownloadUrl();
        this.status = doc.getStatus();
        this.category = doc.getCategory();
        this.expiryDate = doc.getExpiryDate();
        this.createdAt = doc.getCreatedAt();
        this.updatedAt = doc.getUpdatedAt();
        if (doc.getOwner() != null) {
            this.employeeId = doc.getOwner().getId();
            this.employeeName = doc.getOwner().getFullName();
        }
        if (doc.getUploadedBy() != null) {
            this.uploadedByEmail = doc.getUploadedBy().getWorkEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getUploadedByEmail() { return uploadedByEmail; }
    public void setUploadedByEmail(String uploadedByEmail) { this.uploadedByEmail = uploadedByEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
