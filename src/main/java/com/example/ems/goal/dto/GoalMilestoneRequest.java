package com.example.ems.goal.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class GoalMilestoneRequest {

    @NotBlank(message = "Milestone name is required")
    private String name;

    private String description;
    private LocalDate targetDate;
    private Integer weightage = 1;
    private String status = "PENDING";

    public GoalMilestoneRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public Integer getWeightage() { return weightage; }
    public void setWeightage(Integer weightage) { this.weightage = weightage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
