package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class MyGoalItem {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "EMP101")
    private String goalCode;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "string")
    private String category;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String priority;
    @Schema(example = "100.00")
    private Double progressPercentage;
    @Schema(example = "string")
    private String dueDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGoalCode() { return goalCode; }
    public void setGoalCode(String goalCode) { this.goalCode = goalCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
