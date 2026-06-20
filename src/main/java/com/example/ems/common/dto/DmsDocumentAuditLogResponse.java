package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.common.entity.DmsDocumentAuditLog;

import java.time.LocalDateTime;

public class DmsDocumentAuditLogResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "string")
    private String action;
    @Schema(example = "john.doe@example.com")
    private String performedByEmail;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime performedAt;
    @Schema(example = "string")
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
