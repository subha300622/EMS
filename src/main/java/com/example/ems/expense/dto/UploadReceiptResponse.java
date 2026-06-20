package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class UploadReceiptResponse {
    @Schema(example = "1")
    private Long receiptId;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "string")
    private String fileType;
    @Schema(example = "10")
    private long fileSize;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime uploadedAt;

    public UploadReceiptResponse() {}

    public UploadReceiptResponse(Long receiptId, String fileName, String fileType, long fileSize, LocalDateTime uploadedAt) {
        this.receiptId = receiptId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
