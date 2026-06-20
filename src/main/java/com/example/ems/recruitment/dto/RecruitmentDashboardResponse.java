package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class RecruitmentDashboardResponse {
    @Schema(example = "1")
    private long totalJobs;
    @Schema(example = "1")
    private long activeJobs;
    @Schema(example = "1")
    private long closedJobs;
    @Schema(example = "1")
    private long draftJobs;
    @Schema(example = "1")
    private long totalCandidates;
    @Schema(example = "1")
    private long candidatesApplied;
    @Schema(example = "1")
    private long candidatesScreening;
    @Schema(example = "1")
    private long candidatesInterviewing;
    @Schema(example = "1")
    private long candidatesOffered;
    @Schema(example = "1")
    private long candidatesHired;
    @Schema(example = "1")
    private long candidatesRejected;
    @Schema(example = "1")
    private long scheduledInterviewsCount;
    @Schema(example = "100.00")
    private double conversionRate; // hired / total candidates

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }

    public long getClosedJobs() { return closedJobs; }
    public void setClosedJobs(long closedJobs) { this.closedJobs = closedJobs; }

    public long getDraftJobs() { return draftJobs; }
    public void setDraftJobs(long draftJobs) { this.draftJobs = draftJobs; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getCandidatesApplied() { return candidatesApplied; }
    public void setCandidatesApplied(long candidatesApplied) { this.candidatesApplied = candidatesApplied; }

    public long getCandidatesScreening() { return candidatesScreening; }
    public void setCandidatesScreening(long candidatesScreening) { this.candidatesScreening = candidatesScreening; }

    public long getCandidatesInterviewing() { return candidatesInterviewing; }
    public void setCandidatesInterviewing(long candidatesInterviewing) { this.candidatesInterviewing = candidatesInterviewing; }

    public long getCandidatesOffered() { return candidatesOffered; }
    public void setCandidatesOffered(long candidatesOffered) { this.candidatesOffered = candidatesOffered; }

    public long getCandidatesHired() { return candidatesHired; }
    public void setCandidatesHired(long candidatesHired) { this.candidatesHired = candidatesHired; }

    public long getCandidatesRejected() { return candidatesRejected; }
    public void setCandidatesRejected(long candidatesRejected) { this.candidatesRejected = candidatesRejected; }

    public long getScheduledInterviewsCount() { return scheduledInterviewsCount; }
    public void setScheduledInterviewsCount(long scheduledInterviewsCount) { this.scheduledInterviewsCount = scheduledInterviewsCount; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
}
