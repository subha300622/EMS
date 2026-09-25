package com.example.ems.support.dto;

public class SupportEscalationHistoryResponse {

    private Integer level;
    private String reason;
    private String triggeredAt;
    private String action;
    private String status;

    public SupportEscalationHistoryResponse() {}

    public SupportEscalationHistoryResponse(Integer level, String reason, String triggeredAt, String action, String status) {
        this.level = level;
        this.reason = reason;
        this.triggeredAt = triggeredAt;
        this.action = action;
        this.status = status;
    }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(String triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
