package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.common.entity.DmsDocumentVersion;

import java.time.LocalDateTime;

public class DmsDocumentVersionResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "1")
    private Integer versionNumber;
    @Schema(example = "string")
    private String fileName;
    @Schema(example = "10")
    private Long fileSize;
    @Schema(example = "string")
    private String downloadUrl;
    @Schema(example = "john.doe@example.com")
    private String uploadedByEmail;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime uploadedAt;
    @Schema(example = "string")
    private String changeNotes;

    public DmsDocumentVersionResponse() {}

    public DmsDocumentVersionResponse(DmsDocumentVersion version) {
        this.id = version.getId();
        this.versionNumber = version.getVersionNumber();
        this.fileName = version.getFileName();
        this.fileSize = version.getFileSize();
        this.downloadUrl = version.getDownloadUrl();
        this.uploadedAt = version.getUploadedAt();
        this.changeNotes = version.getChangeNotes();
        if (version.getDocument() != null) {
            this.documentId = version.getDocument().getId();
        }
        if (version.getUploadedBy() != null) {
            this.uploadedByEmail = version.getUploadedBy().getWorkEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getUploadedByEmail() { return uploadedByEmail; }
    public void setUploadedByEmail(String uploadedByEmail) { this.uploadedByEmail = uploadedByEmail; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getChangeNotes() { return changeNotes; }
    public void setChangeNotes(String changeNotes) { this.changeNotes = changeNotes; }
}
