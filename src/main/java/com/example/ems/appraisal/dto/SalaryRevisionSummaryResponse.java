package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionSummaryResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "ACTIVE")
    private String status;

    public SalaryRevisionSummaryResponse() {}

    public SalaryRevisionSummaryResponse(String revisionId, String employeeId, String employeeName, BigDecimal incrementPercentage, BigDecimal newSalary, LocalDate effectiveDate, String status) {
        this.revisionId = revisionId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.incrementPercentage = incrementPercentage;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.status = status;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
