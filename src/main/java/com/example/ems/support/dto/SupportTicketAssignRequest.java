package com.example.ems.support.dto;

import jakarta.validation.constraints.NotNull;

public class SupportTicketAssignRequest {

    @NotNull(message = "assignedToId is required")
    private Long assignedToId;

    private String reason;

    public SupportTicketAssignRequest() {}

    public SupportTicketAssignRequest(Long assignedToId, String reason) {
        this.assignedToId = assignedToId;
        this.reason = reason;
    }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
