package com.example.ems.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_support_attachments")
public class MySupportAttachment {

    @Id
    private String fileId;

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    /** Raw file bytes stored in DB so we can serve them on download. */
    @Lob
    @Column(name = "file_content")
    private byte[] fileContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private MySupportTicket ticket;

    public MySupportAttachment() {}

    public MySupportAttachment(String fileId, String fileName, String fileType, Long fileSize, MySupportTicket ticket) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.ticket = ticket;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public byte[] getFileContent() { return fileContent; }
    public void setFileContent(byte[] fileContent) { this.fileContent = fileContent; }

    public MySupportTicket getTicket() { return ticket; }
    public void setTicket(MySupportTicket ticket) { this.ticket = ticket; }
}
