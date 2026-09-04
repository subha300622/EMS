package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;

public class RevenueTrendResponse {
    private String period;
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
    private BigDecimal taxCollected;
    private BigDecimal discountAmount;
    private BigDecimal refundAmount;

    public RevenueTrendResponse() {}

    public RevenueTrendResponse(String period, BigDecimal grossRevenue, BigDecimal netRevenue, BigDecimal taxCollected, BigDecimal discountAmount, BigDecimal refundAmount) {
        this.period = period;
        this.grossRevenue = grossRevenue;
        this.netRevenue = netRevenue;
        this.taxCollected = taxCollected;
        this.discountAmount = discountAmount;
        this.refundAmount = refundAmount;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getGrossRevenue() { return grossRevenue; }
    public void setGrossRevenue(BigDecimal grossRevenue) { this.grossRevenue = grossRevenue; }

    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }

    public BigDecimal getTaxCollected() { return taxCollected; }
    public void setTaxCollected(BigDecimal taxCollected) { this.taxCollected = taxCollected; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
}
