package com.example.ems.dto;

import com.example.ems.entity.PerformancePip;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PerformancePipResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String employeeDesignation;
    private String employeeDepartment;
    private String improvementPlan;
    private LocalDate startDate;
    private LocalDate endDate;
    private long durationDays;
    private long daysRemaining;
    private String status;
    private String outcome;
    private LocalDateTime createdAt;
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
