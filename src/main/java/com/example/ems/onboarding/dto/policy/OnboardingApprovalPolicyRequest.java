package com.example.ems.onboarding.dto.policy;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class OnboardingApprovalPolicyRequest {

    @NotBlank(message = "Current status is required")
    private String currentStatus;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Next status is required")
    private String nextStatus;

    private Map<String, Object> approver;
    private List<String> conditions;
    private boolean active = true;

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNextStatus() { return nextStatus; }
    public void setNextStatus(String nextStatus) { this.nextStatus = nextStatus; }

    public Map<String, Object> getApprover() { return approver; }
    public void setApprover(Map<String, Object> approver) { this.approver = approver; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
