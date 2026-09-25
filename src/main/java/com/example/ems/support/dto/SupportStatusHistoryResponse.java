package com.example.ems.support.dto;

public class SupportStatusHistoryResponse {

    private String fromStatus;
    private String toStatus;
    private String changedBy;
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
