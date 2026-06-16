package com.example.ems.appraisal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionSummaryResponse {

    private String revisionId;
    private String employeeId;
    private String employeeName;
    private BigDecimal incrementPercentage;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
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
