package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SupportDashboardSummaryResponse {

    private long total;

    @JsonProperty("new")
    private long newCount;

    private long inProgress;
    private long resolved;
    private long closed;
    private long overdue;
    private long escalated;
    private long critical;
    private long high;

    public SupportDashboardSummaryResponse() {}

    public SupportDashboardSummaryResponse(long total, long newCount, long inProgress, long resolved, long closed, long overdue, long escalated, long critical, long high) {
        this.total = total;
        this.newCount = newCount;
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.closed = closed;
        this.overdue = overdue;
        this.escalated = escalated;
        this.critical = critical;
        this.high = high;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getNewCount() { return newCount; }
    public void setNewCount(long newCount) { this.newCount = newCount; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }

    public long getClosed() { return closed; }
    public void setClosed(long closed) { this.closed = closed; }

    public long getOverdue() { return overdue; }
    public void setOverdue(long overdue) { this.overdue = overdue; }

    public long getEscalated() { return escalated; }
    public void setEscalated(long escalated) { this.escalated = escalated; }

    public long getCritical() { return critical; }
    public void setCritical(long critical) { this.critical = critical; }

    public long getHigh() { return high; }
    public void setHigh(long high) { this.high = high; }

    // Helper for Jackson reflection if needed
    @JsonProperty("escalated")
    public long getEscalatedValue() { return escalated; }
    @JsonProperty("escalated")
    public void setEscalatedValue(long val) { this.escalated = val; }
}
