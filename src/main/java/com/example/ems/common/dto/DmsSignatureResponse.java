package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.common.entity.DmsDocumentSignature;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DmsSignatureResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "Project Deliverables")
    private String documentTitle;
    @Schema(example = "1")
    private Long requestedFromEmployeeId;
    @Schema(example = "string")
    private String requestedFromEmployeeName;
    @Schema(example = "john.doe@example.com")
    private String requestedByEmail;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime signedAt;
    @Schema(example = "2026-06-19")
    private LocalDate signatureDate;
    @Schema(example = "Excellent progress")
    private String comments;

    public DmsSignatureResponse() {}

    public DmsSignatureResponse(DmsDocumentSignature sig) {
        this.id = sig.getId();
        this.status = sig.getStatus();
        this.signedAt = sig.getSignedAt();
        this.signatureDate = sig.getSignatureDate();
        this.comments = sig.getComments();
        if (sig.getDocument() != null) {
            this.documentId = sig.getDocument().getId();
            this.documentTitle = sig.getDocument().getTitle();
        }
        if (sig.getRequestedFrom() != null) {
            this.requestedFromEmployeeId = sig.getRequestedFrom().getId();
            this.requestedFromEmployeeName = sig.getRequestedFrom().getFullName();
        }
        if (sig.getRequestedBy() != null) {
            this.requestedByEmail = sig.getRequestedBy().getWorkEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public Long getRequestedFromEmployeeId() { return requestedFromEmployeeId; }
    public void setRequestedFromEmployeeId(Long requestedFromEmployeeId) { this.requestedFromEmployeeId = requestedFromEmployeeId; }

    public String getRequestedFromEmployeeName() { return requestedFromEmployeeName; }
    public void setRequestedFromEmployeeName(String requestedFromEmployeeName) { this.requestedFromEmployeeName = requestedFromEmployeeName; }

    public String getRequestedByEmail() { return requestedByEmail; }
    public void setRequestedByEmail(String requestedByEmail) { this.requestedByEmail = requestedByEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public LocalDate getSignatureDate() { return signatureDate; }
    public void setSignatureDate(LocalDate signatureDate) { this.signatureDate = signatureDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
