package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category distribution item response")
public class DistributionResponse {
    @Schema(description = "Category name or range label", example = "Active")
    private String name;

    @Schema(description = "Count or value for this category", example = "42")
    private long count;

    public DistributionResponse() {}

    public DistributionResponse(String name, long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
