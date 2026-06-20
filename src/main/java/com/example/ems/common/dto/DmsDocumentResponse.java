package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.common.entity.DmsDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DmsDocumentResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "string")
    private String fileType;
    @Schema(example = "10")
    private Long fileSize;
    @Schema(example = "string")
    private String downloadUrl;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String category;
    @Schema(example = "2026-06-19")
    private LocalDate expiryDate;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "john.doe@example.com")
    private String uploadedByEmail;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
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
