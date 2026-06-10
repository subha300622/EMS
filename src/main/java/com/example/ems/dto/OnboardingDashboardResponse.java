package com.example.ems.dto;

public class OnboardingDashboardResponse {
    private long totalOnboardings;
    private long pendingOnboardings;
    private long inProgressOnboardings;
    private long completedOnboardings;
    private long approvedOnboardings;
    private long totalTasksAssigned;
    private long completedTasksCount;
    private double taskCompletionRate; // completed / total tasks
    private long pendingVerifications; // documents with PENDING status

    public long getTotalOnboardings() { return totalOnboardings; }
    public void setTotalOnboardings(long totalOnboardings) { this.totalOnboardings = totalOnboardings; }

    public long getPendingOnboardings() { return pendingOnboardings; }
    public void setPendingOnboardings(long pendingOnboardings) { this.pendingOnboardings = pendingOnboardings; }

    public long getInProgressOnboardings() { return inProgressOnboardings; }
    public void setInProgressOnboardings(long inProgressOnboardings) { this.inProgressOnboardings = inProgressOnboardings; }

    public long getCompletedOnboardings() { return completedOnboardings; }
    public void setCompletedOnboardings(long completedOnboardings) { this.completedOnboardings = completedOnboardings; }

    public long getApprovedOnboardings() { return approvedOnboardings; }
    public void setApprovedOnboardings(long approvedOnboardings) { this.approvedOnboardings = approvedOnboardings; }

    public long getTotalTasksAssigned() { return totalTasksAssigned; }
    public void setTotalTasksAssigned(long totalTasksAssigned) { this.totalTasksAssigned = totalTasksAssigned; }

    public long getCompletedTasksCount() { return completedTasksCount; }
    public void setCompletedTasksCount(long completedTasksCount) { this.completedTasksCount = completedTasksCount; }

    public double getTaskCompletionRate() { return taskCompletionRate; }
    public void setTaskCompletionRate(double taskCompletionRate) { this.taskCompletionRate = taskCompletionRate; }

    public long getPendingVerifications() { return pendingVerifications; }
    public void setPendingVerifications(long pendingVerifications) { this.pendingVerifications = pendingVerifications; }
}
