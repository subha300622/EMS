package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionDetailedResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "120000.00")
    private BigDecimal currentSalary;
    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;
    @Schema(example = "5000.00")
    private BigDecimal incrementAmount;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "Personal business")
    private String reason;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
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
