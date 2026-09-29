package com.example.ems.onboarding.dto;

public class OnboardingAssignTemplateRequest {

    private String templateId;
    private boolean regenerateWorkflow;

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public boolean isRegenerateWorkflow() { return regenerateWorkflow; }
    public void setRegenerateWorkflow(boolean regenerateWorkflow) { this.regenerateWorkflow = regenerateWorkflow; }
}
