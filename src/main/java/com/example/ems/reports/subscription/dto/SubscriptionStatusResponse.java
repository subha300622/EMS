package com.example.ems.reports.subscription.dto;

public class SubscriptionStatusResponse {
    private long active;
    private long trial;
    private long expired;
    private long cancelled;
    private long suspended;

    public SubscriptionStatusResponse() {}

    public SubscriptionStatusResponse(long active, long trial, long expired, long cancelled, long suspended) {
        this.active = active;
        this.trial = trial;
        this.expired = expired;
        this.cancelled = cancelled;
        this.suspended = suspended;
    }

    public long getActive() { return active; }
    public void setActive(long active) { this.active = active; }

    public long getTrial() { return trial; }
    public void setTrial(long trial) { this.trial = trial; }

    public long getExpired() { return expired; }
    public void setExpired(long expired) { this.expired = expired; }

    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }

    public long getSuspended() { return suspended; }
    public void setSuspended(long suspended) { this.suspended = suspended; }
}
