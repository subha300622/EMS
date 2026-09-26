package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform Category Analytics Response")
public class PlatformCategoryAnalyticsResponse {
    @Schema(description = "Category Name", example = "Technical Support")
    private String category;
    @Schema(description = "Total number of tickets", example = "45")
    private long ticketCount;
    @Schema(description = "Percentage of total tickets", example = "45.0")
    private double percentage;
    @Schema(description = "Average resolution time", example = "2.5 hours")
    private String avgResolutionTime;
    @Schema(description = "Number of open tickets", example = "5")
    private long openTickets;
    @Schema(description = "Number of closed tickets", example = "40")
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
