package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Report Summary Response")
public class TrainingReportSummaryResponse {
    @Schema(description = "Total number of trainings", example = "50")
    private long totalTrainings;
    @Schema(description = "Number of completed trainings", example = "35")
    private long completedTrainings;
    @Schema(description = "Number of cancelled trainings", example = "2")
    private long cancelledTrainings;
    @Schema(description = "Number of upcoming trainings", example = "10")
    private long upcomingTrainings;
    @Schema(description = "Number of ongoing trainings", example = "3")
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
