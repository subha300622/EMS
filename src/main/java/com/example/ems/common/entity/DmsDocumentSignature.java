package com.example.ems.common.entity;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dms_document_signatures")
public class DmsDocumentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private DmsDocument document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_from_id", nullable = false)
    private Employee requestedFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, SIGNED, DECLINED

    private LocalDateTime signedAt;

    private LocalDate signatureDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    public DmsDocumentSignature() {}

    public DmsDocumentSignature(Long id, DmsDocument document, Employee requestedFrom, User requestedBy, String status, LocalDateTime signedAt, LocalDate signatureDate, String comments) {
        this.id = id;
        this.document = document;
        this.requestedFrom = requestedFrom;
        this.requestedBy = requestedBy;
        this.status = status;
        this.signedAt = signedAt;
        this.signatureDate = signatureDate;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DmsDocument getDocument() { return document; }
    public void setDocument(DmsDocument document) { this.document = document; }

    public Employee getRequestedFrom() { return requestedFrom; }
    public void setRequestedFrom(Employee requestedFrom) { this.requestedFrom = requestedFrom; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public LocalDate getSignatureDate() { return signatureDate; }
    public void setSignatureDate(LocalDate signatureDate) { this.signatureDate = signatureDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
