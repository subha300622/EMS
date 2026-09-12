package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform Category Statistics Response")
public class PlatformCategoryStatsResponse {
    @Schema(description = "Total number of categories", example = "10")
    private long totalCategories;
    @Schema(description = "Number of active categories", example = "8")
    private long activeCategories;
    @Schema(description = "Number of inactive categories", example = "2")
    private long inactiveCategories;
    @Schema(description = "Details about the most frequently used category")
    private MostUsedCategory mostUsedCategory;

    public PlatformCategoryStatsResponse() {}

    public PlatformCategoryStatsResponse(long totalCategories, long activeCategories, long inactiveCategories, MostUsedCategory mostUsedCategory) {
        this.totalCategories = totalCategories;
        this.activeCategories = activeCategories;
        this.inactiveCategories = inactiveCategories;
        this.mostUsedCategory = mostUsedCategory;
    }

    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }

    public long getActiveCategories() { return activeCategories; }
    public void setActiveCategories(long activeCategories) { this.activeCategories = activeCategories; }

    public long getInactiveCategories() { return inactiveCategories; }
    public void setInactiveCategories(long inactiveCategories) { this.inactiveCategories = inactiveCategories; }

    public MostUsedCategory getMostUsedCategory() { return mostUsedCategory; }
    public void setMostUsedCategory(MostUsedCategory mostUsedCategory) { this.mostUsedCategory = mostUsedCategory; }

    public static class MostUsedCategory {
        private Long id;
        private String name;
        private long ticketCount;
        private double percentage;

        public MostUsedCategory() {}

        public MostUsedCategory(Long id, String name, long ticketCount, double percentage) {
            this.id = id;
            this.name = name;
            this.ticketCount = ticketCount;
            this.percentage = percentage;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public long getTicketCount() { return ticketCount; }
        public void setTicketCount(long ticketCount) { this.ticketCount = ticketCount; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}
