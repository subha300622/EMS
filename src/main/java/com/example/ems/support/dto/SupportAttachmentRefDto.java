package com.example.ems.support.dto;

public class SupportAttachmentRefDto {
    private String fileId;

    public SupportAttachmentRefDto() {}

    public SupportAttachmentRefDto(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
}
