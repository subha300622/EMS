package com.example.ems.appraisal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionDetailedResponse {

    private String revisionId;
    private String employeeId;
    private String employeeName;
    private BigDecimal currentSalary;
    private BigDecimal incrementPercentage;
    private BigDecimal incrementAmount;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String reason;
    private String status;
    private String createdAt;

    public SalaryRevisionDetailedResponse() {}

    public SalaryRevisionDetailedResponse(String revisionId, String employeeId, String employeeName, BigDecimal currentSalary, BigDecimal incrementPercentage, BigDecimal incrementAmount, BigDecimal newSalary, LocalDate effectiveDate, String reason, String status, String createdAt) {
        this.revisionId = revisionId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.currentSalary = currentSalary;
        this.incrementPercentage = incrementPercentage;
        this.incrementAmount = incrementAmount;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

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

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
