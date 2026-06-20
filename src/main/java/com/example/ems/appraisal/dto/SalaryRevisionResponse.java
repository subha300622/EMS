package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.appraisal.entity.SalaryRevision;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalaryRevisionResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "120000.00")
    private BigDecimal previousSalary;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "100.00")
    private BigDecimal changePercentage;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "Personal business")
    private String reason;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;

    public SalaryRevisionResponse() {}

    public SalaryRevisionResponse(SalaryRevision sr) {
        this.id = sr.getId();
        this.previousSalary = sr.getPreviousSalary();
        this.newSalary = sr.getNewSalary();
        this.changePercentage = sr.getChangePercentage();
        this.effectiveDate = sr.getEffectiveDate();
        this.reason = sr.getReason();
        this.createdAt = sr.getCreatedAt();

        if (sr.getEmployee() != null) {
            this.employeeId = sr.getEmployee().getId();
            this.employeeName = sr.getEmployee().getFullName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public BigDecimal getPreviousSalary() { return previousSalary; }
    public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public BigDecimal getChangePercentage() { return changePercentage; }
    public void setChangePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
