package com.example.ems.schedule.dto;

public class TeamScheduleOverviewDto {
    private double coveragePercentage; // e.g. 94.2
    private int systemAlertsCount; // e.g. 1
    private int pendingSwapRequests; // e.g. 2
    private int totalShifts; // e.g. 38
    private double coverageTargetPercentage; // e.g. 91.0
    private double overtimeHours; // e.g. 42.0
    private int swapRequestsCount; // e.g. 2

    public TeamScheduleOverviewDto() {}

    public TeamScheduleOverviewDto(double coveragePercentage, int systemAlertsCount, int pendingSwapRequests,
                                   int totalShifts, double coverageTargetPercentage, double overtimeHours,
                                   int swapRequestsCount) {
        this.coveragePercentage = coveragePercentage;
        this.systemAlertsCount = systemAlertsCount;
        this.pendingSwapRequests = pendingSwapRequests;
        this.totalShifts = totalShifts;
        this.coverageTargetPercentage = coverageTargetPercentage;
        this.overtimeHours = overtimeHours;
        this.swapRequestsCount = swapRequestsCount;
    }

    public double getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(double coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public int getSystemAlertsCount() {
        return systemAlertsCount;
    }

    public void setSystemAlertsCount(int systemAlertsCount) {
        this.systemAlertsCount = systemAlertsCount;
    }

    public int getPendingSwapRequests() {
        return pendingSwapRequests;
    }

    public void setPendingSwapRequests(int pendingSwapRequests) {
        this.pendingSwapRequests = pendingSwapRequests;
    }

    public int getTotalShifts() {
        return totalShifts;
    }

    public void setTotalShifts(int totalShifts) {
        this.totalShifts = totalShifts;
    }

    public double getCoverageTargetPercentage() {
        return coverageTargetPercentage;
    }

    public void setCoverageTargetPercentage(double coverageTargetPercentage) {
        this.coverageTargetPercentage = coverageTargetPercentage;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public int getSwapRequestsCount() {
        return swapRequestsCount;
    }

    public void setSwapRequestsCount(int swapRequestsCount) {
        this.swapRequestsCount = swapRequestsCount;
    }
}
