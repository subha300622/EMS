package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public class UploadReceiptResponse {
    private Long receiptId;
    private String fileName;
    private String fileType;
    private long fileSize;
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
