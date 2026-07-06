package com.example.ems.reports.revenue.dto;

import java.util.List;

public class RevenueDashboardResponse {
    private RevenueSummaryResponse kpis;
    private List<RevenueTrendResponse> trends;
    private List<RevenueGrowthResponse> growth;
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
