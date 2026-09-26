package com.example.ems.storage.dto;

import com.example.ems.storage.entity.FileMetadata;
import com.example.ems.storage.entity.FileType;
import java.time.LocalDateTime;

public class FileMetadataResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private FileType fileType;
    private String uploadedByUserId;
    private String uploadedByRole;
    private String departmentId;
    private LocalDateTime createdAt;

    public FileMetadataResponse() {}

    public FileMetadataResponse(FileMetadata metadata) {
        if (metadata != null) {
            this.id = metadata.getId();
            this.fileName = metadata.getFileName();
            this.filePath = metadata.getFilePath();
            this.fileType = metadata.getFileType();
            this.uploadedByUserId = metadata.getUploadedByUserId();
            this.uploadedByRole = metadata.getUploadedByRole();
            this.departmentId = metadata.getDepartmentId();
            this.createdAt = metadata.getCreatedAt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }

    public String getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(String uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }

    public String getUploadedByRole() { return uploadedByRole; }
    public void setUploadedByRole(String uploadedByRole) { this.uploadedByRole = uploadedByRole; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
