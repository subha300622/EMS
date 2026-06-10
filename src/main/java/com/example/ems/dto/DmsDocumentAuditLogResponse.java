package com.example.ems.dto;

import com.example.ems.entity.DmsDocumentAuditLog;
import java.time.LocalDateTime;

public class DmsDocumentAuditLogResponse {
    private Long id;
    private Long documentId;
    private String action;
    private String performedByEmail;
    private LocalDateTime performedAt;
    private String details;

    public DmsDocumentAuditLogResponse() {}

    public DmsDocumentAuditLogResponse(DmsDocumentAuditLog log) {
        this.id = log.getId();
        this.action = log.getAction();
        this.performedAt = log.getPerformedAt();
        this.details = log.getDetails();
        if (log.getDocument() != null) {
            this.documentId = log.getDocument().getId();
        }
        if (log.getPerformedBy() != null) {
            this.performedByEmail = log.getPerformedBy().getWorkEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedByEmail() { return performedByEmail; }
    public void setPerformedByEmail(String performedByEmail) { this.performedByEmail = performedByEmail; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
