package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue KPI summary metrics response")
public class RevenueSummaryResponse {
    @Schema(description = "Total Gross Revenue", example = "100000.00")
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Schema(description = "Net Revenue", example = "95000.00")
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Schema(description = "Collected Cash Revenue", example = "92000.00")
    private BigDecimal collectedRevenue = BigDecimal.ZERO;

    @Schema(description = "Pending / Unpaid Revenue", example = "8000.00")
    private BigDecimal pendingRevenue = BigDecimal.ZERO;

    @Schema(description = "Failed Payments Count", example = "2")
    private Long failedPayments = 0L;

    @Schema(description = "Total Refund Amount", example = "3000.00")
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Schema(description = "Taxes Collected", example = "18000.00")
    private BigDecimal taxesCollected = BigDecimal.ZERO;

    @Schema(description = "Discounts Applied", example = "5000.00")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Schema(description = "Monthly Recurring Revenue (MRR)", example = "8500.00")
    private BigDecimal mrr = BigDecimal.ZERO;

    @Schema(description = "Annual Recurring Revenue (ARR)", example = "102000.00")
    private BigDecimal arr = BigDecimal.ZERO;

    @Schema(description = "Average Revenue Per User (ARPU)", example = "24.50")
    private BigDecimal arpu = BigDecimal.ZERO;

    @Schema(description = "Average Revenue Per Account (ARPA)", example = "2040.00")
    private BigDecimal arpa = BigDecimal.ZERO;

    @Schema(description = "Customer Lifetime Value (LTV)", example = "12500.00")
    private BigDecimal ltv = BigDecimal.ZERO;

    @Schema(description = "Month-over-month revenue growth percentage", example = "8.5")
    private Double revenueGrowthPercent = 0.0;

    @Schema(description = "Refund Rate Percentage", example = "2.1")
    private Double refundRatePercent = 0.0;

    @Schema(description = "Collection Rate Percentage", example = "96.4")
    private Double collectionRatePercent = 0.0;

    @Schema(description = "Discount Impact Percentage", example = "4.2")
    private Double discountImpactPercent = 0.0;

    @Schema(description = "Tax Impact Percentage", example = "18.0")
    private Double taxImpactPercent = 0.0;

    @Schema(description = "Average Invoice Value", example = "1500.00")
    private BigDecimal averageInvoiceValue = BigDecimal.ZERO;

    @Schema(description = "Average Payment Value", example = "1500.00")
    private BigDecimal averagePaymentValue = BigDecimal.ZERO;

    @Schema(description = "Forecasted Revenue for Next Period", example = "110000.00")
    private BigDecimal forecastRevenue = BigDecimal.ZERO;

    @Schema(description = "Net Retention Rate (NRR)", example = "112.0")
    private Double nrr = 0.0;

    @Schema(description = "Gross Retention Rate (GRR)", example = "94.5")
    private Double grr = 0.0;

    @Schema(description = "Churn Rate Percentage", example = "1.8")
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
