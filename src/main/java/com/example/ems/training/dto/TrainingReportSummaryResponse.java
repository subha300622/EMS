package com.example.ems.training.dto;

public class TrainingReportSummaryResponse {
    private long totalTrainings;
    private long completedTrainings;
    private long cancelledTrainings;
    private long upcomingTrainings;
    private long ongoingTrainings;

    public long getTotalTrainings() { return totalTrainings; }
    public void setTotalTrainings(long totalTrainings) { this.totalTrainings = totalTrainings; }

    public long getCompletedTrainings() { return completedTrainings; }
    public void setCompletedTrainings(long completedTrainings) { this.completedTrainings = completedTrainings; }

    public long getCancelledTrainings() { return cancelledTrainings; }
    public void setCancelledTrainings(long cancelledTrainings) { this.cancelledTrainings = cancelledTrainings; }

    public long getUpcomingTrainings() { return upcomingTrainings; }
    public void setUpcomingTrainings(long upcomingTrainings) { this.upcomingTrainings = upcomingTrainings; }

    public long getOngoingTrainings() { return ongoingTrainings; }
    public void setOngoingTrainings(long ongoingTrainings) { this.ongoingTrainings = ongoingTrainings; }
}
