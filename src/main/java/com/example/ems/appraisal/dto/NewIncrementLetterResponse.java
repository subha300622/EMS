package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewIncrementLetterResponse {

    @Schema(example = "string")
    private String letterId;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "Software Engineer")
    private String designation;
    @Schema(example = "Engineering")
    private String department;
    @Schema(example = "120000.00")
    private BigDecimal previousSalary;
    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;
    @Schema(example = "5000.00")
    private BigDecimal incrementAmount;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "string")
    private String generatedAt;

    public NewIncrementLetterResponse() {}

    public NewIncrementLetterResponse(String letterId, String employeeId, String employeeName, String designation, String department, BigDecimal previousSalary, BigDecimal incrementPercentage, BigDecimal incrementAmount, BigDecimal newSalary, LocalDate effectiveDate, String generatedAt) {
        this.letterId = letterId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.designation = designation;
        this.department = department;
        this.previousSalary = previousSalary;
        this.incrementPercentage = incrementPercentage;
        this.incrementAmount = incrementAmount;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.generatedAt = generatedAt;
    }

    public String getLetterId() { return letterId; }
    public void setLetterId(String letterId) { this.letterId = letterId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public BigDecimal getPreviousSalary() { return previousSalary; }
    public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
