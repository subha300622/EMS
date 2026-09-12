package com.example.ems.goal.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class GoalEffortRequest {

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @NotNull(message = "Hours are required")
    private Double hours;

    private String description;

    public GoalEffortRequest() {}

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
