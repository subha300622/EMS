package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Dashboard Metrics Response")
public class TrainingDashboardMetricsResponse {
    @Schema(description = "Total number of trainings", example = "25")
    private long totalTrainings;
    @Schema(description = "Number of draft trainings", example = "4")
    private long draftTrainings;
    @Schema(description = "Number of trainings pending approval", example = "2")
    private long pendingApproval;
    @Schema(description = "Number of upcoming trainings", example = "8")
    private long upcomingTrainings;
    @Schema(description = "Number of ongoing trainings", example = "3")
    private long ongoingTrainings;
    @Schema(description = "Number of completed trainings", example = "7")
    private long completedTrainings;
    @Schema(description = "Number of cancelled trainings", example = "1")
    private long cancelledTrainings;
    @Schema(description = "Number of sessions scheduled for today", example = "2")
    private long todaysSessions;
    @Schema(description = "Number of trainings assigned to the current user", example = "5")
    private long myAssignedTrainings;
    @Schema(description = "Number of pending responses for the current user", example = "1")
    private long myPendingResponses;

    public long getTotalTrainings() { return totalTrainings; }
    public void setTotalTrainings(long totalTrainings) { this.totalTrainings = totalTrainings; }

    public long getDraftTrainings() { return draftTrainings; }
    public void setDraftTrainings(long draftTrainings) { this.draftTrainings = draftTrainings; }

    public long getPendingApproval() { return pendingApproval; }
    public void setPendingApproval(long pendingApproval) { this.pendingApproval = pendingApproval; }

    public long getUpcomingTrainings() { return upcomingTrainings; }
    public void setUpcomingTrainings(long upcomingTrainings) { this.upcomingTrainings = upcomingTrainings; }

    public long getOngoingTrainings() { return ongoingTrainings; }
    public void setOngoingTrainings(long ongoingTrainings) { this.ongoingTrainings = ongoingTrainings; }

    public long getCompletedTrainings() { return completedTrainings; }
    public void setCompletedTrainings(long completedTrainings) { this.completedTrainings = completedTrainings; }

    public long getCancelledTrainings() { return cancelledTrainings; }
    public void setCancelledTrainings(long cancelledTrainings) { this.cancelledTrainings = cancelledTrainings; }

    public long getTodaysSessions() { return todaysSessions; }
    public void setTodaysSessions(long todaysSessions) { this.todaysSessions = todaysSessions; }

    public long getMyAssignedTrainings() { return myAssignedTrainings; }
    public void setMyAssignedTrainings(long myAssignedTrainings) { this.myAssignedTrainings = myAssignedTrainings; }

    public long getMyPendingResponses() { return myPendingResponses; }
    public void setMyPendingResponses(long myPendingResponses) { this.myPendingResponses = myPendingResponses; }
}
