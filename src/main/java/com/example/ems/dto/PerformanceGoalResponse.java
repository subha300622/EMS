package com.example.ems.dto;

import com.example.ems.entity.PerformanceGoal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PerformanceGoalResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long cycleId;
    private String cycleName;
    private String title;
    private String description;
    private int progressPercent;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PerformanceGoalResponse() {}

    public PerformanceGoalResponse(PerformanceGoal g) {
        this.id = g.getId();
        this.title = g.getTitle();
        this.description = g.getDescription();
        this.progressPercent = g.getProgressPercent();
        this.status = g.getStatus();
        this.dueDate = g.getDueDate();
        this.createdAt = g.getCreatedAt();
        this.updatedAt = g.getUpdatedAt();
        if (g.getEmployee() != null) {
            this.employeeId = g.getEmployee().getId();
            this.employeeName = g.getEmployee().getFullName();
        }
        if (g.getCycle() != null) {
            this.cycleId = g.getCycle().getId();
            this.cycleName = g.getCycle().getName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }
    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
