package com.example.ems.onboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class OnboardingDashboardResponse {
    @Schema(example = "1")
    private long totalOnboardings;
    @Schema(example = "1")
    private long pendingOnboardings;
    @Schema(example = "75")
    private long inProgressOnboardings;
    @Schema(example = "1")
    private long completedOnboardings;
    @Schema(example = "1")
    private long approvedOnboardings;
    @Schema(example = "1")
    private long totalTasksAssigned;
    @Schema(example = "1")
    private long completedTasksCount;
    @Schema(example = "100.00")
    private double taskCompletionRate; // completed / total tasks
    @Schema(example = "1")
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
