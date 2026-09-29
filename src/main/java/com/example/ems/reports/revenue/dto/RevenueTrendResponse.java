package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue trend entry response")
public class RevenueTrendResponse {
    @Schema(description = "Time Period label", example = "2026-06")
    private String period;

    @Schema(description = "Gross Revenue", example = "80000.00")
    private BigDecimal grossRevenue;

    @Schema(description = "Net Revenue", example = "75000.00")
    private BigDecimal netRevenue;

    @Schema(description = "Tax Collected", example = "12000.00")
    private BigDecimal taxCollected;

    @Schema(description = "Discounts Applied", example = "5000.00")
    private BigDecimal discountAmount;

    @Schema(description = "Refund Amount", example = "0.00")
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
