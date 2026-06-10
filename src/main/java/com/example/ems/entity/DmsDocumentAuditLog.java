package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dms_document_audit_logs")
public class DmsDocumentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private DmsDocument document;

    @Column(nullable = false)
    private String action; // UPLOADED, DOWNLOADED, APPROVED, REJECTED, SHARED, VERSION_ADDED, SIGNATURE_REQUESTED, SIGNED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String details;

    public DmsDocumentAuditLog() {}

    public DmsDocumentAuditLog(Long id, DmsDocument document, String action, User performedBy, LocalDateTime performedAt, String details) {
        this.id = id;
        this.document = document;
        this.action = action;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DmsDocument getDocument() { return document; }
    public void setDocument(DmsDocument document) { this.document = document; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
