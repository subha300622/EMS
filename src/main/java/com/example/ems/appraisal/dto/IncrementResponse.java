package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.Increment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IncrementResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long appraisalId;
    private BigDecimal currentSalary;
    private BigDecimal incrementPercentage;
    private BigDecimal incrementAmount;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String status;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;

    public IncrementResponse() {}

    public IncrementResponse(Increment inc) {
        this.id = inc.getId();
        this.currentSalary = inc.getCurrentSalary();
        this.incrementPercentage = inc.getIncrementPercentage();
        this.incrementAmount = inc.getIncrementAmount();
        this.newSalary = inc.getNewSalary();
        this.effectiveDate = inc.getEffectiveDate();
        this.status = inc.getStatus();
        this.approvedAt = inc.getApprovedAt();
        this.appliedAt = inc.getAppliedAt();
        this.createdAt = inc.getCreatedAt();

        if (inc.getEmployee() != null) {
            this.employeeId = inc.getEmployee().getId();
            this.employeeName = inc.getEmployee().getFullName();
            this.employeeEmail = inc.getEmployee().getEmail();
        }

        if (inc.getAppraisal() != null) {
            this.appraisalId = inc.getAppraisal().getId();
        }

        if (inc.getApprovedBy() != null) {
            this.approvedById = inc.getApprovedBy().getId();
            this.approvedByName = inc.getApprovedBy().getFullName();
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

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
