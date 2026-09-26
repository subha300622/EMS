package com.example.ems.reports.subscription.dto;

import java.math.BigDecimal;

public class RevenueReportEntry {
    private String period;
    private BigDecimal monthlyRevenue;
    private BigDecimal annualRevenue;
    private BigDecimal totalRevenue;

    public RevenueReportEntry() {}

    public RevenueReportEntry(String period, BigDecimal monthlyRevenue, BigDecimal annualRevenue, BigDecimal totalRevenue) {
        this.period = period;
        this.monthlyRevenue = monthlyRevenue;
        this.annualRevenue = annualRevenue;
        this.totalRevenue = totalRevenue;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
