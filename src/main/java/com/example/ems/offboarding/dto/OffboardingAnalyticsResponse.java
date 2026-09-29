package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate statistics and metrics for the offboarding dashboard")
public class OffboardingAnalyticsResponse {

    @Schema(description = "Count of currently active offboarding processes", example = "5")
    private long active;

    @Schema(description = "Count of completed offboarding processes", example = "12")
    private long completed;

    @Schema(description = "Count of future scheduled exits", example = "3")
    private long scheduled;

    @Schema(description = "Total offboarding requests recorded", example = "20")
    private long totalRequests;

    @Schema(description = "Count of voluntary employee resignations", example = "16")
    private long voluntaryExits;

    @Schema(description = "Count of involuntary terminations/separations", example = "4")
    private long involuntaryExits;

    @Schema(description = "Average notice period served in days", example = "30.0")
    private Double averageNoticePeriodDays;

    @Schema(description = "Average total duration in days to complete offboarding", example = "28.0")
    private Double averageExitCompletionDays;

    public OffboardingAnalyticsResponse() {
        this.active = 0;
        this.completed = 0;
        this.scheduled = 0;
        this.totalRequests = 0;
        this.voluntaryExits = 0;
        this.involuntaryExits = 0;
        this.averageNoticePeriodDays = 0.0;
        this.averageExitCompletionDays = 0.0;
    }

    public OffboardingAnalyticsResponse(long active, long completed, long scheduled, long totalRequests,
                                      long voluntaryExits, long involuntaryExits,
                                      Double averageNoticePeriodDays, Double averageExitCompletionDays) {
        this.active = active;
        this.completed = completed;
        this.scheduled = scheduled;
        this.totalRequests = totalRequests;
        this.voluntaryExits = voluntaryExits;
        this.involuntaryExits = involuntaryExits;
        this.averageNoticePeriodDays = averageNoticePeriodDays != null ? averageNoticePeriodDays : 0.0;
        this.averageExitCompletionDays = averageExitCompletionDays != null ? averageExitCompletionDays : 0.0;
    }

    public long getActive() { return active; }
    public void setActive(long active) { this.active = active; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getScheduled() { return scheduled; }
    public void setScheduled(long scheduled) { this.scheduled = scheduled; }

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public long getVoluntaryExits() { return voluntaryExits; }
    public void setVoluntaryExits(long voluntaryExits) { this.voluntaryExits = voluntaryExits; }

    public long getInvoluntaryExits() { return involuntaryExits; }
    public void setInvoluntaryExits(long involuntaryExits) { this.involuntaryExits = involuntaryExits; }

    public Double getAverageNoticePeriodDays() { return averageNoticePeriodDays; }
    public void setAverageNoticePeriodDays(Double averageNoticePeriodDays) {
        this.averageNoticePeriodDays = averageNoticePeriodDays != null ? averageNoticePeriodDays : 0.0;
    }

    public Double getAverageExitCompletionDays() { return averageExitCompletionDays; }
    public void setAverageExitCompletionDays(Double averageExitCompletionDays) {
        this.averageExitCompletionDays = averageExitCompletionDays != null ? averageExitCompletionDays : 0.0;
    }
}
