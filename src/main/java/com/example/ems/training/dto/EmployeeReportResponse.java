package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee Training Report Response")
public class EmployeeReportResponse {
    @Schema(description = "Employee ID", example = "EMP001")
    private String employeeId;
    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;
    @Schema(description = "Total number of assigned trainings", example = "10")
    private long totalTrainings;
    @Schema(description = "Number of mandatory trainings", example = "6")
    private long mandatoryTrainings;
    @Schema(description = "Number of completed trainings", example = "8")
    private long completed;
    @Schema(description = "Number of trainings in progress", example = "1")
    private long inProgress;
    @Schema(description = "Number of pending trainings", example = "1")
    private long pending;
    @Schema(description = "Number of overdue trainings", example = "0")
    private long overdue;
    @Schema(description = "Percentage of trainings completed", example = "80.00")
    private double completionPercentage;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public long getTotalTrainings() { return totalTrainings; }
    public void setTotalTrainings(long totalTrainings) { this.totalTrainings = totalTrainings; }

    public long getMandatoryTrainings() { return mandatoryTrainings; }
    public void setMandatoryTrainings(long mandatoryTrainings) { this.mandatoryTrainings = mandatoryTrainings; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getOverdue() { return overdue; }
    public void setOverdue(long overdue) { this.overdue = overdue; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
