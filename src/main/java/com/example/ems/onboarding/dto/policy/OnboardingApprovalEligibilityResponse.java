package com.example.ems.onboarding.dto.policy;

import java.util.List;
import java.util.Map;

public class OnboardingApprovalEligibilityResponse {

    private Long onboardingId;
    private String currentStatus;
    private String action;
    private String nextStatus;
    private boolean eligible;
    private Map<String, Object> approvalRule;
    private List<Map<String, Object>> eligibleApprovers;
    private List<String> pendingConditions;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNextStatus() { return nextStatus; }
    public void setNextStatus(String nextStatus) { this.nextStatus = nextStatus; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public Map<String, Object> getApprovalRule() { return approvalRule; }
    public void setApprovalRule(Map<String, Object> approvalRule) { this.approvalRule = approvalRule; }

    public List<Map<String, Object>> getEligibleApprovers() { return eligibleApprovers; }
    public void setEligibleApprovers(List<Map<String, Object>> eligibleApprovers) { this.eligibleApprovers = eligibleApprovers; }

    public List<String> getPendingConditions() { return pendingConditions; }
    public void setPendingConditions(List<String> pendingConditions) { this.pendingConditions = pendingConditions; }
}
