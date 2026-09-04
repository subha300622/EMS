package com.example.ems.training.dto;

public class EmployeeReportResponse {
    private String employeeId;
    private String employeeName;
    private long totalTrainings;
    private long mandatoryTrainings;
    private long completed;
    private long inProgress;
    private long pending;
    private long overdue;
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
