package com.example.ems.common.dto;

import com.example.ems.common.entity.DmsDocumentShare;

import java.time.LocalDateTime;

public class DmsDocumentShareResponse {
    private Long id;
    private Long documentId;
    private Long sharedWithEmployeeId;
    private String sharedWithEmployeeName;
    private String sharedByEmail;
    private LocalDateTime sharedAt;
    private String accessLevel;

    public DmsDocumentShareResponse() {}

    public DmsDocumentShareResponse(DmsDocumentShare share) {
        this.id = share.getId();
        this.sharedAt = share.getSharedAt();
        this.accessLevel = share.getAccessLevel();
        if (share.getDocument() != null) {
            this.documentId = share.getDocument().getId();
        }
        if (share.getSharedWith() != null) {
            this.sharedWithEmployeeId = share.getSharedWith().getId();
            this.sharedWithEmployeeName = share.getSharedWith().getFullName();
        }
        if (share.getSharedBy() != null) {
            this.sharedByEmail = share.getSharedBy().getWorkEmail();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getSharedWithEmployeeId() { return sharedWithEmployeeId; }
    public void setSharedWithEmployeeId(Long sharedWithEmployeeId) { this.sharedWithEmployeeId = sharedWithEmployeeId; }

    public String getSharedWithEmployeeName() { return sharedWithEmployeeName; }
    public void setSharedWithEmployeeName(String sharedWithEmployeeName) { this.sharedWithEmployeeName = sharedWithEmployeeName; }

    public String getSharedByEmail() { return sharedByEmail; }
    public void setSharedByEmail(String sharedByEmail) { this.sharedByEmail = sharedByEmail; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
