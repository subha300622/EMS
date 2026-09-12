package com.example.ems.support.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;

public class PlatformBulkActionRequest {

    @NotEmpty(message = "Ticket IDs are required")
    private List<Long> ticketIds;

    private String status;
    private String priority;
    private String agentId;

    public PlatformBulkActionRequest() {}

    public List<Long> getTicketIds() { return ticketIds; }
    public void setTicketIds(List<Long> ticketIds) { this.ticketIds = ticketIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}
