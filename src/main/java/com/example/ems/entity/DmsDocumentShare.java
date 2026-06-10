package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dms_document_shares")
public class DmsDocumentShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private DmsDocument document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_with_id", nullable = false)
    private Employee sharedWith;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_by_id", nullable = false)
    private User sharedBy;

    private LocalDateTime sharedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String accessLevel = "READ"; // READ, WRITE

    public DmsDocumentShare() {}

    public DmsDocumentShare(Long id, DmsDocument document, Employee sharedWith, User sharedBy, LocalDateTime sharedAt, String accessLevel) {
        this.id = id;
        this.document = document;
        this.sharedWith = sharedWith;
        this.sharedBy = sharedBy;
        this.sharedAt = sharedAt;
        this.accessLevel = accessLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DmsDocument getDocument() { return document; }
    public void setDocument(DmsDocument document) { this.document = document; }

    public Employee getSharedWith() { return sharedWith; }
    public void setSharedWith(Employee sharedWith) { this.sharedWith = sharedWith; }

    public User getSharedBy() { return sharedBy; }
    public void setSharedBy(User sharedBy) { this.sharedBy = sharedBy; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
