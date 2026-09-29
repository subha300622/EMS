package com.example.ems.support.dto;

import java.util.List;
import java.util.Map;

public class PlatformSupportDashboardResponse {

    private Map<String, Object> summary;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> priorityBreakdown;
    private Map<String, Long> categoryBreakdown;
    private List<Map<String, Object>> monthlyTrend;
    private List<Map<String, Object>> recentTickets;
    private Map<String, Object> slaMetrics;

    public PlatformSupportDashboardResponse() {}

    public PlatformSupportDashboardResponse(Map<String, Object> summary, Map<String, Long> statusBreakdown,
                                            Map<String, Long> priorityBreakdown, Map<String, Long> categoryBreakdown,
                                            List<Map<String, Object>> monthlyTrend, List<Map<String, Object>> recentTickets,
                                            Map<String, Object> slaMetrics) {
        this.summary = summary;
        this.statusBreakdown = statusBreakdown;
        this.priorityBreakdown = priorityBreakdown;
        this.categoryBreakdown = categoryBreakdown;
        this.monthlyTrend = monthlyTrend;
        this.recentTickets = recentTickets;
        this.slaMetrics = slaMetrics;
    }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }

    public Map<String, Long> getPriorityBreakdown() { return priorityBreakdown; }
    public void setPriorityBreakdown(Map<String, Long> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; }

    public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<Map<String, Object>> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<Map<String, Object>> monthlyTrend) { this.monthlyTrend = monthlyTrend; }

    public List<Map<String, Object>> getRecentTickets() { return recentTickets; }
    public void setRecentTickets(List<Map<String, Object>> recentTickets) { this.recentTickets = recentTickets; }

    public Map<String, Object> getSlaMetrics() { return slaMetrics; }
    public void setSlaMetrics(Map<String, Object> slaMetrics) { this.slaMetrics = slaMetrics; }
}
