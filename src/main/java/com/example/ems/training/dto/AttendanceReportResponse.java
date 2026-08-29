package com.example.ems.training.dto;

public class AttendanceReportResponse {
    private String departmentName;
    private long totalAssigned;
    private long totalAttended;
    private long totalAbsent;
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
