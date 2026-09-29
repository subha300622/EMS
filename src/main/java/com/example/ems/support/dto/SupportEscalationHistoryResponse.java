package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support Ticket Escalation History")
public class SupportEscalationHistoryResponse {

    @Schema(description = "Escalation Level", example = "1")
    private Integer level;

    @Schema(description = "Reason for escalation", example = "Approaching SLA resolution deadline")
    private String reason;

    @Schema(description = "Timestamp when escalation was triggered", example = "2026-09-25T12:00:00Z")
    private String triggeredAt;

    @Schema(description = "Action taken", example = "Reassigned to Tier 2 Lead")
    private String action;

    @Schema(description = "Escalation status", example = "ESCALATED")
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
