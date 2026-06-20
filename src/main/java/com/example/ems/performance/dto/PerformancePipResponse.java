package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.performance.entity.PerformancePip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PerformancePipResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "john.doe@example.com")
    private String employeeEmail;
    @Schema(example = "Software Engineer")
    private String employeeDesignation;
    @Schema(example = "Engineering")
    private String employeeDepartment;
    @Schema(example = "string")
    private String improvementPlan;
    @Schema(example = "2026-06-19")
    private LocalDate startDate;
    @Schema(example = "2026-06-19")
    private LocalDate endDate;
    @Schema(example = "1")
    private long durationDays;
    @Schema(example = "1")
    private long daysRemaining;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String outcome;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public PerformancePipResponse() {}

    public PerformancePipResponse(PerformancePip pip) {
        this.id = pip.getId();
        this.improvementPlan = pip.getImprovementPlan();
        this.startDate = pip.getStartDate();
        this.endDate = pip.getEndDate();
        this.status = pip.getStatus();
        this.outcome = pip.getOutcome();
        this.createdAt = pip.getCreatedAt();
        this.updatedAt = pip.getUpdatedAt();

        if (pip.getStartDate() != null && pip.getEndDate() != null) {
            this.durationDays = ChronoUnit.DAYS.between(pip.getStartDate(), pip.getEndDate());
        }
        if (pip.getEndDate() != null) {
            long remaining = ChronoUnit.DAYS.between(LocalDate.now(), pip.getEndDate());
            this.daysRemaining = Math.max(0, remaining);
        }
        if (pip.getEmployee() != null) {
            this.employeeId = pip.getEmployee().getId();
            this.employeeName = pip.getEmployee().getFullName();
            this.employeeEmail = pip.getEmployee().getEmail();
            this.employeeDesignation = pip.getEmployee().getDesignation();
            this.employeeDepartment = pip.getEmployee().getDepartment();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    public String getEmployeeDesignation() { return employeeDesignation; }
    public void setEmployeeDesignation(String employeeDesignation) { this.employeeDesignation = employeeDesignation; }
    public String getEmployeeDepartment() { return employeeDepartment; }
    public void setEmployeeDepartment(String employeeDepartment) { this.employeeDepartment = employeeDepartment; }
    public String getImprovementPlan() { return improvementPlan; }
    public void setImprovementPlan(String improvementPlan) { this.improvementPlan = improvementPlan; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public long getDurationDays() { return durationDays; }
    public void setDurationDays(long durationDays) { this.durationDays = durationDays; }
    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
