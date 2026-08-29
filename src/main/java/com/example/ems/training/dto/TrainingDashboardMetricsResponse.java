package com.example.ems.training.dto;

public class TrainingDashboardMetricsResponse {
    private long totalTrainings;
    private long draftTrainings;
    private long pendingApproval;
    private long upcomingTrainings;
    private long ongoingTrainings;
    private long completedTrainings;
    private long cancelledTrainings;
    private long todaysSessions;
    private long myAssignedTrainings;
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
