package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class UpdateGoalRequest {
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "string")
    private String priority;
    @Schema(example = "1")
    private Integer weightage;
    @Schema(example = "2026-06-19")
    private LocalDate targetDate;
    @Schema(example = "2026-06-19")
    private LocalDate startDate;
    @Schema(example = "string")
    private String goalType;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getWeightage() { return weightage; }
    public void setWeightage(Integer weightage) { this.weightage = weightage; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
}
