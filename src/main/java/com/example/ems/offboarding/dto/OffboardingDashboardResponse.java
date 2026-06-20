package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class OffboardingDashboardResponse {
    @Schema(example = "1")
    private long totalOffboardings;
    @Schema(example = "1")
    private long pendingOffboardings;
    @Schema(example = "75")
    private long inProgressOffboardings;
    @Schema(example = "1")
    private long completedOffboardings;
    @Schema(example = "1")
    private long approvedOffboardings;
    @Schema(example = "1")
    private long rejectedOffboardings;
    @Schema(example = "1")
    private long totalTasksAssigned;
    @Schema(example = "1")
    private long completedTasksCount;
    @Schema(example = "100.00")
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
