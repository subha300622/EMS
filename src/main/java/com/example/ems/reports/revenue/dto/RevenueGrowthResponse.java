package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;

public class RevenueGrowthResponse {
    private String period;
    private BigDecimal currentRevenue;
    private BigDecimal previousRevenue;
    private Double growthRate;

    public RevenueGrowthResponse() {}

    public RevenueGrowthResponse(String period, BigDecimal currentRevenue, BigDecimal previousRevenue, Double growthRate) {
        this.period = period;
        this.currentRevenue = currentRevenue;
        this.previousRevenue = previousRevenue;
        this.growthRate = growthRate;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getCurrentRevenue() { return currentRevenue; }
    public void setCurrentRevenue(BigDecimal currentRevenue) { this.currentRevenue = currentRevenue; }

    public BigDecimal getPreviousRevenue() { return previousRevenue; }
    public void setPreviousRevenue(BigDecimal previousRevenue) { this.previousRevenue = previousRevenue; }

    public Double getGrowthRate() { return growthRate; }
    public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }
}
