package com.example.ems.support.dto;

public class PlatformCategoryAnalyticsResponse {
    private String category;
    private long ticketCount;
    private double percentage;
    private String avgResolutionTime;
    private long openTickets;
    private long closedTickets;

    public PlatformCategoryAnalyticsResponse() {}

    public PlatformCategoryAnalyticsResponse(String category, long ticketCount, double percentage, String avgResolutionTime, long openTickets, long closedTickets) {
        this.category = category;
        this.ticketCount = ticketCount;
        this.percentage = percentage;
        this.avgResolutionTime = avgResolutionTime;
        this.openTickets = openTickets;
        this.closedTickets = closedTickets;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getTicketCount() { return ticketCount; }
    public void setTicketCount(long ticketCount) { this.ticketCount = ticketCount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getAvgResolutionTime() { return avgResolutionTime; }
    public void setAvgResolutionTime(String avgResolutionTime) { this.avgResolutionTime = avgResolutionTime; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

    public long getClosedTickets() { return closedTickets; }
    public void setClosedTickets(long closedTickets) { this.closedTickets = closedTickets; }
}
