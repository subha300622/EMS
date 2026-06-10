package com.example.ems.dto;

public class RecruitmentDashboardResponse {
    private long totalJobs;
    private long activeJobs;
    private long closedJobs;
    private long draftJobs;
    private long totalCandidates;
    private long candidatesApplied;
    private long candidatesScreening;
    private long candidatesInterviewing;
    private long candidatesOffered;
    private long candidatesHired;
    private long candidatesRejected;
    private long scheduledInterviewsCount;
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
