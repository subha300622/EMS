package com.example.ems.offboarding.dto;

public class OffboardingDashboardResponse {
    private long totalOffboardings;
    private long pendingOffboardings;
    private long inProgressOffboardings;
    private long completedOffboardings;
    private long approvedOffboardings;
    private long rejectedOffboardings;
    private long totalTasksAssigned;
    private long completedTasksCount;
    private double taskCompletionRate; // completed / total

    public long getTotalOffboardings() { return totalOffboardings; }
    public void setTotalOnboardings(long totalOffboardings) { this.totalOffboardings = totalOffboardings; }

    public long getPendingOffboardings() { return pendingOffboardings; }
    public void setPendingOffboardings(long pendingOffboardings) { this.pendingOffboardings = pendingOffboardings; }

    public long getInProgressOffboardings() { return inProgressOffboardings; }
    public void setInProgressOffboardings(long inProgressOffboardings) { this.inProgressOffboardings = inProgressOffboardings; }

    public long getCompletedOffboardings() { return completedOffboardings; }
    public void setCompletedOffboardings(long completedOffboardings) { this.completedOffboardings = completedOffboardings; }

    public long getApprovedOffboardings() { return approvedOffboardings; }
    public void setApprovedOffboardings(long approvedOffboardings) { this.approvedOffboardings = approvedOffboardings; }

    public long getRejectedOffboardings() { return rejectedOffboardings; }
    public void setRejectedOffboardings(long rejectedOffboardings) { this.rejectedOffboardings = rejectedOffboardings; }

    public long getTotalTasksAssigned() { return totalTasksAssigned; }
    public void setTotalTasksAssigned(long totalTasksAssigned) { this.totalTasksAssigned = totalTasksAssigned; }

    public long getCompletedTasksCount() { return completedTasksCount; }
    public void setCompletedTasksCount(long completedTasksCount) { this.completedTasksCount = completedTasksCount; }

    public double getTaskCompletionRate() { return taskCompletionRate; }
    public void setTaskCompletionRate(double taskCompletionRate) { this.taskCompletionRate = taskCompletionRate; }
}
