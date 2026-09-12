package com.example.ems.goal.dto;

import java.time.LocalDate;

public class UpdateGoalRequest {

    private String goalName;
    private String description;
    private String category;
    private String type;
    private String priority;
    private Integer weightage;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double targetValue;
    private String unitOfMeasurement;
    private Long ownerId;
    private Double estimatedHours;
    private String visibility;
    private Long parentGoalId;
    private Long projectId;

    public UpdateGoalRequest() {}

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getWeightage() { return weightage; }
    public void setWeightage(Integer weightage) { this.weightage = weightage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

    public String getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(String unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Long getParentGoalId() { return parentGoalId; }
    public void setParentGoalId(Long parentGoalId) { this.parentGoalId = parentGoalId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
