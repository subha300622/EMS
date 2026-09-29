package com.example.ems.support.dto;

public class SlaDashboardResponse {

    private SummaryDto summary;
    private TicketMetricsDto ticketMetrics;

    public SlaDashboardResponse() {}

    public SlaDashboardResponse(SummaryDto summary, TicketMetricsDto ticketMetrics) {
        this.summary = summary;
        this.ticketMetrics = ticketMetrics;
    }

    public SummaryDto getSummary() { return summary; }
    public void setSummary(SummaryDto summary) { this.summary = summary; }

    public TicketMetricsDto getTicketMetrics() { return ticketMetrics; }
    public void setTicketMetrics(TicketMetricsDto ticketMetrics) { this.ticketMetrics = ticketMetrics; }

    public static class SummaryDto {
        private long totalPolicies;
        private long activePolicies;
        private long inactivePolicies;
        private String defaultPolicy;

        public SummaryDto(long totalPolicies, long activePolicies, long inactivePolicies, String defaultPolicy) {
            this.totalPolicies = totalPolicies;
            this.activePolicies = activePolicies;
            this.inactivePolicies = inactivePolicies;
            this.defaultPolicy = defaultPolicy;
        }

        public long getTotalPolicies() { return totalPolicies; }
        public long getActivePolicies() { return activePolicies; }
        public long getInactivePolicies() { return inactivePolicies; }
        public String getDefaultPolicy() { return defaultPolicy; }
    }

    public static class TicketMetricsDto {
        private long withinSla;
        private long breached;
        private double compliancePercentage;

        public TicketMetricsDto(long withinSla, long breached, double compliancePercentage) {
            this.withinSla = withinSla;
            this.breached = breached;
            this.compliancePercentage = compliancePercentage;
        }

        public long getWithinSla() { return withinSla; }
        public long getBreached() { return breached; }
        public double getCompliancePercentage() { return compliancePercentage; }
    }
}
