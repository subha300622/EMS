package com.example.ems.performance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateGoalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Goal type is required")
    private String goalType; // INDIVIDUAL, DEPARTMENT, ORGANIZATIONAL

    @NotBlank(message = "Priority is required")
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    private Integer weightage;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Target date is required")
    private LocalDate targetDate;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private Long managerId;

    private List<KeyResultItem> keyResults;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getWeightage() { return weightage; }
    public void setWeightage(Integer weightage) { this.weightage = weightage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public List<KeyResultItem> getKeyResults() { return keyResults; }
    public void setKeyResults(List<KeyResultItem> keyResults) { this.keyResults = keyResults; }

    public static class KeyResultItem {
        private String title;
        private Integer targetValue;
        private String unit;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getTargetValue() { return targetValue; }
        public void setTargetValue(Integer targetValue) { this.targetValue = targetValue; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}
