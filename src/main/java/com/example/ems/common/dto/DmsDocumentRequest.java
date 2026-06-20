package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class DmsDocumentRequest {

    @NotBlank(message = "Document title is required")
    @Schema(example = "Project Deliverables")
    private String title;

    @Schema(example = "Detailed description of the item")
    private String description;

    @NotBlank(message = "Category is required")
    @Schema(example = "string")
    private String category; // VISA, CONTRACT, IDENTIFICATION, POLICY

    @Schema(example = "2026-06-19")
    private LocalDate expiryDate;

    @NotNull(message = "Owner employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotBlank(message = "File name is required")
    @Schema(example = "string")
    private String fileName;

    @NotBlank(message = "File type is required")
    @Schema(example = "string")
    private String fileType;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    @Schema(example = "10")
    private Long fileSize;

    @NotBlank(message = "Download URL is required")
    @Schema(example = "string")
    private String downloadUrl;

    public DmsDocumentRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
