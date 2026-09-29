package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Attendance Report Response")
public class AttendanceReportResponse {
    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;
    @Schema(description = "Total number of participants assigned", example = "20")
    private long totalAssigned;
    @Schema(description = "Total number of participants attended", example = "18")
    private long totalAttended;
    @Schema(description = "Total number of participants absent", example = "2")
    private long totalAbsent;
    @Schema(description = "Percentage of participants completed", example = "90.00")
    private double completionPercentage;

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public long getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(long totalAssigned) { this.totalAssigned = totalAssigned; }

    public long getTotalAttended() { return totalAttended; }
    public void setTotalAttended(long totalAttended) { this.totalAttended = totalAttended; }

    public long getTotalAbsent() { return totalAbsent; }
    public void setTotalAbsent(long totalAbsent) { this.totalAbsent = totalAbsent; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
