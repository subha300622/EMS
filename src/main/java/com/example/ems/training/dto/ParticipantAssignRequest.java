package com.example.ems.training.dto;

import com.example.ems.training.entity.AssignmentTargetType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ParticipantAssignRequest {

    @NotNull(message = "Assignment target type is required")
    private AssignmentTargetType assignmentType;

    private Long departmentId;
    private Long teamId;
    private Long designationId;
    private List<Long> employeeIds;

    private Boolean sendNotification = true;

    public AssignmentTargetType getAssignmentType() { return assignmentType; }
    public void setAssignmentType(AssignmentTargetType assignmentType) { this.assignmentType = assignmentType; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }

    public List<Long> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }

    public Boolean getSendNotification() { return sendNotification; }
    public void setSendNotification(Boolean sendNotification) { this.sendNotification = sendNotification; }
}
