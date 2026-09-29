package com.example.ems.goal.dto;

import jakarta.validation.constraints.NotBlank;

public class GoalAssignRequest {

    private Long assignedToEmployeeId;
    private Long departmentId;
    private Long teamId;
    private Long branchId;
    private Long roleId;

    @NotBlank(message = "Assignment level is required")
    private String assignmentLevel; // ORGANIZATION, BRANCH, DEPARTMENT, TEAM, EMPLOYEE, PROJECT

    @NotBlank(message = "Assignment type is required")
    private String assignmentType; // MANUAL, DEPARTMENT_RULE, TEAM_RULE, ROLE_RULE, MANAGER_RULE, CASCADE, REASSIGNMENT

    private String reason;

    public GoalAssignRequest() {}

    public Long getAssignedToEmployeeId() { return assignedToEmployeeId; }
    public void setAssignedToEmployeeId(Long assignedToEmployeeId) { this.assignedToEmployeeId = assignedToEmployeeId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getAssignmentLevel() { return assignmentLevel; }
    public void setAssignmentLevel(String assignmentLevel) { this.assignmentLevel = assignmentLevel; }

    public String getAssignmentType() { return assignmentType; }
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
