package com.example.ems.onboarding.dto.policy;

public class OnboardingStatusTransitionPolicyDto {

    private String policyId;
    private String currentStatus;
    private String action;
    private String nextStatus;
    private String approverType;
    private boolean organizationConfigurable;

    public OnboardingStatusTransitionPolicyDto() {}

    public OnboardingStatusTransitionPolicyDto(String policyId, String currentStatus, String action, String nextStatus, String approverType, boolean organizationConfigurable) {
        this.policyId = policyId;
        this.currentStatus = currentStatus;
        this.action = action;
        this.nextStatus = nextStatus;
        this.approverType = approverType;
        this.organizationConfigurable = organizationConfigurable;
    }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNextStatus() { return nextStatus; }
    public void setNextStatus(String nextStatus) { this.nextStatus = nextStatus; }

    public String getApproverType() { return approverType; }
    public void setApproverType(String approverType) { this.approverType = approverType; }

    public boolean isOrganizationConfigurable() { return organizationConfigurable; }
    public void setOrganizationConfigurable(boolean organizationConfigurable) { this.organizationConfigurable = organizationConfigurable; }
}
