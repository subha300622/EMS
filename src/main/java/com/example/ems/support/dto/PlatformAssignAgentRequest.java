package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;

public class PlatformAssignAgentRequest {

    @NotBlank(message = "Agent ID or work email is required")
    private String agentId;

    private String reason;

    public PlatformAssignAgentRequest() {}

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
