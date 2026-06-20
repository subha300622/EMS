package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncrementLetterResponse {
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "Engineering")
    private String department;
    @Schema(example = "Software Engineer")
    private String designation;
    @Schema(example = "120000.00")
    private BigDecimal currentSalary;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;
    @Schema(example = "5000.00")
    private BigDecimal incrementAmount;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "string")
    private String letterBody;

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getLetterBody() { return letterBody; }
    public void setLetterBody(String letterBody) { this.letterBody = letterBody; }
}
