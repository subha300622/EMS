package com.example.ems.reports.subscription.dto;

public class SubscriptionChurnResponse {
    private double churnRate;
    private long cancelledSubscriptions;
    private long renewedSubscriptions;
    private double retentionRate;

    public SubscriptionChurnResponse() {}

    public SubscriptionChurnResponse(double churnRate, long cancelledSubscriptions, long renewedSubscriptions, double retentionRate) {
        this.churnRate = churnRate;
        this.cancelledSubscriptions = cancelledSubscriptions;
        this.renewedSubscriptions = renewedSubscriptions;
        this.retentionRate = retentionRate;
    }

    public double getChurnRate() { return churnRate; }
    public void setChurnRate(double churnRate) { this.churnRate = churnRate; }

    public long getCancelledSubscriptions() { return cancelledSubscriptions; }
    public void setCancelledSubscriptions(long cancelledSubscriptions) { this.cancelledSubscriptions = cancelledSubscriptions; }

    public long getRenewedSubscriptions() { return renewedSubscriptions; }
    public void setRenewedSubscriptions(long renewedSubscriptions) { this.renewedSubscriptions = renewedSubscriptions; }

    public double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
}
