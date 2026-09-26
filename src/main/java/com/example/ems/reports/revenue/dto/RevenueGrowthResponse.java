package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue growth analysis response")
public class RevenueGrowthResponse {
    @Schema(description = "Time Period label", example = "2026-Q2")
    private String period;

    @Schema(description = "Current Period Revenue", example = "85000.00")
    private BigDecimal currentRevenue;

    @Schema(description = "Previous Period Revenue", example = "78000.00")
    private BigDecimal previousRevenue;

    @Schema(description = "Growth Rate Percentage", example = "8.97")
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
