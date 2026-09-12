package com.example.ems.training.dto;

import com.example.ems.training.entity.AssignmentTargetType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TrainingUnifiedAssignmentRequest {

    @NotNull(message = "Assignment target type is required")
    private AssignmentTargetType assignmentType; // DEPARTMENT, TEAM, EMPLOYEE

    @NotNull(message = "Target IDs list is required")
    private List<String> targetIds;

    private Boolean mandatory = true;

    private LocalDate dueDate;

    public AssignmentTargetType getAssignmentType() { return assignmentType; }
    public void setAssignmentType(AssignmentTargetType assignmentType) { this.assignmentType = assignmentType; }

    public List<String> getTargetIds() { return targetIds; }
    public void setTargetIds(List<String> targetIds) { this.targetIds = targetIds; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
