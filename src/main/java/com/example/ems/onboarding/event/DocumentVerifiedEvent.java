package com.example.ems.onboarding.event;

import org.springframework.context.ApplicationEvent;

public class DocumentVerifiedEvent extends ApplicationEvent {
    private final Long documentId;
    private final String status;
    private final String notes;

    public DocumentVerifiedEvent(Object source, Long documentId, String status, String notes) {
        super(source);
        this.documentId = documentId;
        this.status = status;
        this.notes = notes;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
