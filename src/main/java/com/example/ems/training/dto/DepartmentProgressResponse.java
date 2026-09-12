package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department Training Progress Response")
public class DepartmentProgressResponse {
    @Schema(description = "Department ID", example = "1")
    private String departmentId;
    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;
    @Schema(description = "Total number of employees in department", example = "50")
    private long totalEmployees;
    @Schema(description = "Number of employees assigned to trainings", example = "45")
    private long assignedEmployees;
    @Schema(description = "Number of completed trainings", example = "30")
    private long completed;
    @Schema(description = "Number of trainings in progress", example = "10")
    private long inProgress;
    @Schema(description = "Number of pending trainings", example = "5")
    private long pending;
    @Schema(description = "Percentage of trainings completed", example = "66.67")
    private double completionPercentage;

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getAssignedEmployees() { return assignedEmployees; }
    public void setAssignedEmployees(long assignedEmployees) { this.assignedEmployees = assignedEmployees; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
