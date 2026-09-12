package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Revenue dashboard analytical overview response")
public class RevenueDashboardResponse {
    @Schema(description = "Revenue key performance indicators")
    private RevenueSummaryResponse kpis;

    @Schema(description = "Historical revenue trend series")
    private List<RevenueTrendResponse> trends;

    @Schema(description = "Period-over-period growth rates")
    private List<RevenueGrowthResponse> growth;

    @Schema(description = "Predictive revenue forecasts")
    private RevenueForecastResponse forecast;

    public RevenueDashboardResponse() {}

    public RevenueDashboardResponse(RevenueSummaryResponse kpis, List<RevenueTrendResponse> trends, List<RevenueGrowthResponse> growth, RevenueForecastResponse forecast) {
        this.kpis = kpis;
        this.trends = trends;
        this.growth = growth;
        this.forecast = forecast;
    }

    public RevenueSummaryResponse getKpis() { return kpis; }
    public void setKpis(RevenueSummaryResponse kpis) { this.kpis = kpis; }

    public List<RevenueTrendResponse> getTrends() { return trends; }
    public void setTrends(List<RevenueTrendResponse> trends) { this.trends = trends; }

    public List<RevenueGrowthResponse> getGrowth() { return growth; }
    public void setGrowth(List<RevenueGrowthResponse> growth) { this.growth = growth; }

    public RevenueForecastResponse getForecast() { return forecast; }
    public void setForecast(RevenueForecastResponse forecast) { this.forecast = forecast; }
}
