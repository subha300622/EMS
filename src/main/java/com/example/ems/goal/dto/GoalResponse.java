package com.example.ems.goal.dto;

import com.example.ems.goal.domain.Goal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalResponse {

    private Long id;
    private Long organizationId;
    private String goalNumber;
    private String goalName;
    private String description;
    private String category;
    private String type;
    private String priority;
    private Integer weightage;
    private Integer progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double targetValue;
    private Double currentValue;
    private String unitOfMeasurement;
    private Long ownerId;
    private Double estimatedHours;
    private Double actualHours;
    private Double hoursVariance;
    private Double efficiencyPercentage;
    private String status;
    private String healthIndicator; // ON_TRACK, AT_RISK, OVERDUE, COMPLETED
    private String visibility;
    private Long parentGoalId;
    private Long projectId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GoalResponse() {}

    public static GoalResponse fromEntity(Goal goal) {
        GoalResponse dto = new GoalResponse();
        dto.setId(goal.getId());
        dto.setOrganizationId(goal.getOrganizationId());
        dto.setGoalNumber(goal.getGoalNumber());
        dto.setGoalName(goal.getGoalName());
        dto.setDescription(goal.getDescription());
        dto.setCategory(goal.getCategory());
        dto.setType(goal.getType());
        dto.setPriority(goal.getPriority());
        dto.setWeightage(goal.getWeightage());
        dto.setProgress(goal.getProgress() != null ? goal.getProgress() : 0);
        dto.setStartDate(goal.getStartDate());
        dto.setEndDate(goal.getEndDate());
        dto.setTargetValue(goal.getTargetValue());
        dto.setCurrentValue(goal.getCurrentValue());
        dto.setUnitOfMeasurement(goal.getUnitOfMeasurement());
        dto.setOwnerId(goal.getOwnerId());
        dto.setEstimatedHours(goal.getEstimatedHours() != null ? goal.getEstimatedHours() : 0.0);
        dto.setActualHours(goal.getActualHours() != null ? goal.getActualHours() : 0.0);
        
        double actual = dto.getActualHours();
        double est = dto.getEstimatedHours();
        dto.setHoursVariance(actual - est);
        dto.setEfficiencyPercentage(actual > 0 ? (est / actual) * 100.0 : 100.0);

        dto.setStatus(goal.getStatus());
        dto.setVisibility(goal.getVisibility());
        dto.setParentGoalId(goal.getParentGoalId());
        dto.setProjectId(goal.getProjectId());
        dto.setCreatedBy(goal.getCreatedBy());
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());

        // Health indicator calculation
        dto.setHealthIndicator(calculateHealth(goal));
        return dto;
    }

    private static String calculateHealth(Goal goal) {
        if ("COMPLETED".equalsIgnoreCase(goal.getStatus())) return "COMPLETED";
        if ("CANCELLED".equalsIgnoreCase(goal.getStatus())) return "CANCELLED";
        if (goal.getEndDate() != null && LocalDate.now().isAfter(goal.getEndDate())) return "OVERDUE";

        if (goal.getStartDate() != null && goal.getEndDate() != null && !goal.getEndDate().isBefore(goal.getStartDate())) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate());
            long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), LocalDate.now());
            if (totalDays > 0 && elapsedDays > 0) {
                double expectedProgress = Math.min(100.0, ((double) elapsedDays / totalDays) * 100.0);
                int actualProgress = goal.getProgress() != null ? goal.getProgress() : 0;
                if (actualProgress < (expectedProgress - 15.0)) {
                    return "AT_RISK";
                }
            }
        }
        return "ON_TRACK";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getGoalNumber() { return goalNumber; }
    public void setGoalNumber(String goalNumber) { this.goalNumber = goalNumber; }

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

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

    public String getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(String unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public Double getHoursVariance() { return hoursVariance; }
    public void setHoursVariance(Double hoursVariance) { this.hoursVariance = hoursVariance; }

    public Double getEfficiencyPercentage() { return efficiencyPercentage; }
    public void setEfficiencyPercentage(Double efficiencyPercentage) { this.efficiencyPercentage = efficiencyPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHealthIndicator() { return healthIndicator; }
    public void setHealthIndicator(String healthIndicator) { this.healthIndicator = healthIndicator; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Long getParentGoalId() { return parentGoalId; }
    public void setParentGoalId(Long parentGoalId) { this.parentGoalId = parentGoalId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
