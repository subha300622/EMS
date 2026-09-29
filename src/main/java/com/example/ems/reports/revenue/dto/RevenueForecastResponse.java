package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Revenue forecast projection response")
public class RevenueForecastResponse {
    @Schema(description = "Forecast Horizon in months", example = "6")
    private int horizonMonths;

    @Schema(description = "Forecast Confidence Score (0-100)", example = "95.0")
    private double forecastConfidenceScore;

    @Schema(description = "Projected data points")
    private List<ForecastDataPoint> dataPoints;

    public RevenueForecastResponse() {}

    public RevenueForecastResponse(int horizonMonths, double forecastConfidenceScore, List<ForecastDataPoint> dataPoints) {
        this.horizonMonths = horizonMonths;
        this.forecastConfidenceScore = forecastConfidenceScore;
        this.dataPoints = dataPoints;
    }

    public int getHorizonMonths() { return horizonMonths; }
    public void setHorizonMonths(int horizonMonths) { this.horizonMonths = horizonMonths; }

    public double getForecastConfidenceScore() { return forecastConfidenceScore; }
    public void setForecastConfidenceScore(double forecastConfidenceScore) { this.forecastConfidenceScore = forecastConfidenceScore; }

    public List<ForecastDataPoint> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<ForecastDataPoint> dataPoints) { this.dataPoints = dataPoints; }

    @Schema(description = "Forecast data point")
    public static class ForecastDataPoint {
        @Schema(description = "Period label", example = "2026-08")
        private String period;

        @Schema(description = "Projected Revenue", example = "105000.00")
        private BigDecimal projectedRevenue;

        public ForecastDataPoint() {}

        public ForecastDataPoint(String period, BigDecimal projectedRevenue) {
            this.period = period;
            this.projectedRevenue = projectedRevenue;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public BigDecimal getProjectedRevenue() { return projectedRevenue; }
        public void setProjectedRevenue(BigDecimal projectedRevenue) { this.projectedRevenue = projectedRevenue; }
    }
}
