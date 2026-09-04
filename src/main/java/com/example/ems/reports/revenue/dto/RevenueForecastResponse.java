package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueForecastResponse {
    private int horizonMonths;
    private double forecastConfidenceScore;
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

    public static class ForecastDataPoint {
        private String period;
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
