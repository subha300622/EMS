package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;

public class RevenueSummaryResponse {
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal netRevenue = BigDecimal.ZERO;
    private BigDecimal collectedRevenue = BigDecimal.ZERO;
    private BigDecimal pendingRevenue = BigDecimal.ZERO;
    private Long failedPayments = 0L;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private BigDecimal taxesCollected = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal mrr = BigDecimal.ZERO;
    private BigDecimal arr = BigDecimal.ZERO;
    private BigDecimal arpu = BigDecimal.ZERO;
    private BigDecimal arpa = BigDecimal.ZERO;
    private BigDecimal ltv = BigDecimal.ZERO;
    private Double revenueGrowthPercent = 0.0;
    private Double refundRatePercent = 0.0;
    private Double collectionRatePercent = 0.0;
    private Double discountImpactPercent = 0.0;
    private Double taxImpactPercent = 0.0;
    private BigDecimal averageInvoiceValue = BigDecimal.ZERO;
    private BigDecimal averagePaymentValue = BigDecimal.ZERO;
    private BigDecimal forecastRevenue = BigDecimal.ZERO;
    private Double nrr = 0.0;
    private Double grr = 0.0;
    private Double churnRate = 0.0;

    public RevenueSummaryResponse() {}

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }

    public BigDecimal getCollectedRevenue() { return collectedRevenue; }
    public void setCollectedRevenue(BigDecimal collectedRevenue) { this.collectedRevenue = collectedRevenue; }

    public BigDecimal getPendingRevenue() { return pendingRevenue; }
    public void setPendingRevenue(BigDecimal pendingRevenue) { this.pendingRevenue = pendingRevenue; }

    public Long getFailedPayments() { return failedPayments; }
    public void setFailedPayments(Long failedPayments) { this.failedPayments = failedPayments; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public BigDecimal getTaxesCollected() { return taxesCollected; }
    public void setTaxesCollected(BigDecimal taxesCollected) { this.taxesCollected = taxesCollected; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getMrr() { return mrr; }
    public void setMrr(BigDecimal mrr) { this.mrr = mrr; }

    public BigDecimal getArr() { return arr; }
    public void setArr(BigDecimal arr) { this.arr = arr; }

    public BigDecimal getArpu() { return arpu; }
    public void setArpu(BigDecimal arpu) { this.arpu = arpu; }

    public BigDecimal getArpa() { return arpa; }
    public void setArpa(BigDecimal arpa) { this.arpa = arpa; }

    public BigDecimal getLtv() { return ltv; }
    public void setLtv(BigDecimal ltv) { this.ltv = ltv; }

    public Double getRevenueGrowthPercent() { return revenueGrowthPercent; }
    public void setRevenueGrowthPercent(Double revenueGrowthPercent) { this.revenueGrowthPercent = revenueGrowthPercent; }

    public Double getRefundRatePercent() { return refundRatePercent; }
    public void setRefundRatePercent(Double refundRatePercent) { this.refundRatePercent = refundRatePercent; }

    public Double getCollectionRatePercent() { return collectionRatePercent; }
    public void setCollectionRatePercent(Double collectionRatePercent) { this.collectionRatePercent = collectionRatePercent; }

    public Double getDiscountImpactPercent() { return discountImpactPercent; }
    public void setDiscountImpactPercent(Double discountImpactPercent) { this.discountImpactPercent = discountImpactPercent; }

    public Double getTaxImpactPercent() { return taxImpactPercent; }
    public void setTaxImpactPercent(Double taxImpactPercent) { this.taxImpactPercent = taxImpactPercent; }

    public BigDecimal getAverageInvoiceValue() { return averageInvoiceValue; }
    public void setAverageInvoiceValue(BigDecimal averageInvoiceValue) { this.averageInvoiceValue = averageInvoiceValue; }

    public BigDecimal getAveragePaymentValue() { return averagePaymentValue; }
    public void setAveragePaymentValue(BigDecimal averagePaymentValue) { this.averagePaymentValue = averagePaymentValue; }

    public BigDecimal getForecastRevenue() { return forecastRevenue; }
    public void setForecastRevenue(BigDecimal forecastRevenue) { this.forecastRevenue = forecastRevenue; }

    public Double getNrr() { return nrr; }
    public void setNrr(Double nrr) { this.nrr = nrr; }

    public Double getGrr() { return grr; }
    public void setGrr(Double grr) { this.grr = grr; }

    public Double getChurnRate() { return churnRate; }
    public void setChurnRate(Double churnRate) { this.churnRate = churnRate; }
}
