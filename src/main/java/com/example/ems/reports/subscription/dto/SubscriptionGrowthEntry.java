package com.example.ems.reports.subscription.dto;

public class SubscriptionGrowthEntry {
    private String date;
    private long newSubscriptions;
    private long renewals;
    private long cancellations;

    public SubscriptionGrowthEntry() {}

    public SubscriptionGrowthEntry(String date, long newSubscriptions, long renewals, long cancellations) {
        this.date = date;
        this.newSubscriptions = newSubscriptions;
        this.renewals = renewals;
        this.cancellations = cancellations;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getNewSubscriptions() { return newSubscriptions; }
    public void setNewSubscriptions(long newSubscriptions) { this.newSubscriptions = newSubscriptions; }

    public long getRenewals() { return renewals; }
    public void setRenewals(long renewals) { this.renewals = renewals; }

    public long getCancellations() { return cancellations; }
    public void setCancellations(long cancellations) { this.cancellations = cancellations; }
}
