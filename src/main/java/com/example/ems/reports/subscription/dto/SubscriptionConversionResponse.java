package com.example.ems.reports.subscription.dto;

public class SubscriptionConversionResponse {
    private long trialOrganizations;
    private long convertedToPaid;
    private double conversionRate;
    private double averageConversionDays;

    public SubscriptionConversionResponse() {}

    public SubscriptionConversionResponse(long trialOrganizations, long convertedToPaid, double conversionRate, double averageConversionDays) {
        this.trialOrganizations = trialOrganizations;
        this.convertedToPaid = convertedToPaid;
        this.conversionRate = conversionRate;
        this.averageConversionDays = averageConversionDays;
    }

    public long getTrialOrganizations() { return trialOrganizations; }
    public void setTrialOrganizations(long trialOrganizations) { this.trialOrganizations = trialOrganizations; }

    public long getConvertedToPaid() { return convertedToPaid; }
    public void setConvertedToPaid(long convertedToPaid) { this.convertedToPaid = convertedToPaid; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

    public double getAverageConversionDays() { return averageConversionDays; }
    public void setAverageConversionDays(double averageConversionDays) { this.averageConversionDays = averageConversionDays; }
}
