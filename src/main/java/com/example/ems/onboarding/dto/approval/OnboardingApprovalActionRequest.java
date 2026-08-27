package com.example.ems.onboarding.dto.approval;

import jakarta.validation.constraints.NotBlank;

public class OnboardingApprovalActionRequest {

    @NotBlank(message = "action is required (APPROVE or REJECT)")
    private String action;

    private String remarks;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
