package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support Ticket Status Transition History")
public class SupportStatusHistoryResponse {

    @Schema(description = "Previous status", example = "NEW")
    private String fromStatus;

    @Schema(description = "New status", example = "IN_PROGRESS")
    private String toStatus;

    @Schema(description = "User who changed the status", example = "support.manager@company.com")
    private String changedBy;

    @Schema(description = "Timestamp of change", example = "2026-09-25T10:15:00Z")
    private String changedAt;

    public SupportStatusHistoryResponse() {}

    public SupportStatusHistoryResponse(String fromStatus, String toStatus, String changedBy, String changedAt) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
}
