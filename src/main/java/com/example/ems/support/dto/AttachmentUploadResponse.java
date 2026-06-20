package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AttachmentUploadResponse {
    @Schema(example = "string")
    private String fileId;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "string")
    private String fileType;
    @Schema(example = "10")
    private Long fileSize;
    @Schema(example = "string")
    private String uploadedAt;

    public AttachmentUploadResponse() {}

    public AttachmentUploadResponse(String fileId, String fileName, String fileType, Long fileSize, String uploadedAt) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
}
