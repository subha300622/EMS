package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.common.entity.DmsDocumentShare;

import java.time.LocalDateTime;

public class DmsDocumentShareResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long documentId;
    @Schema(example = "1")
    private Long sharedWithEmployeeId;
    @Schema(example = "string")
    private String sharedWithEmployeeName;
    @Schema(example = "john.doe@example.com")
    private String sharedByEmail;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime sharedAt;
    @Schema(example = "string")
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
