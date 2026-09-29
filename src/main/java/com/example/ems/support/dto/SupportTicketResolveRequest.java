package com.example.ems.support.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class SupportTicketResolveRequest {

    @NotBlank(message = "Resolution is required")
    private String resolution;

    @DecimalMin(value = "0.0", message = "Actual hours cannot be negative")
    private Double actualHours;

    public SupportTicketResolveRequest() {}

    public SupportTicketResolveRequest(String resolution, Double actualHours) {
        this.resolution = resolution;
        this.actualHours = actualHours;
    }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
}
