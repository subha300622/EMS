package com.example.ems.goal.dto;

import jakarta.validation.constraints.NotBlank;

public class GoalApprovalPolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String policyName;

    @NotBlank(message = "Action is required")
    private String action; // CREATE, COMPLETE

    private Boolean approvalRequired = true;
    private String approvalType = "MANAGER"; // MANAGER, HR, MULTI_LEVEL, ADMIN
    private String priority; // HIGH, CRITICAL, ALL
    private String goalType; // ORGANIZATION, DEPARTMENT, TEAM, INDIVIDUAL, ALL
    private Integer weightageThreshold = 0;
    private Double estimatedHoursThreshold = 0.0;
    private Long departmentId;
    private String approverRole; // REPORTING_MANAGER, DEPARTMENT_HEAD, HR, ADMIN
    private Integer approvalLevels = 1;
    private Boolean autoApproval = false;
    private Boolean isActive = true;

    public GoalApprovalPolicyRequest() {}

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public String getApprovalType() { return approvalType; }
    public void setApprovalType(String approvalType) { this.approvalType = approvalType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public Integer getWeightageThreshold() { return weightageThreshold; }
    public void setWeightageThreshold(Integer weightageThreshold) { this.weightageThreshold = weightageThreshold; }

    public Double getEstimatedHoursThreshold() { return estimatedHoursThreshold; }
    public void setEstimatedHoursThreshold(Double estimatedHoursThreshold) { this.estimatedHoursThreshold = estimatedHoursThreshold; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public Integer getApprovalLevels() { return approvalLevels; }
    public void setApprovalLevels(Integer approvalLevels) { this.approvalLevels = approvalLevels; }

    public Boolean getAutoApproval() { return autoApproval; }
    public void setAutoApproval(Boolean autoApproval) { this.autoApproval = autoApproval; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
