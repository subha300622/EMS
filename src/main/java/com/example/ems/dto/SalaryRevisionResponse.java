package com.example.ems.dto;

import com.example.ems.entity.SalaryRevision;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalaryRevisionResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private BigDecimal previousSalary;
    private BigDecimal newSalary;
    private BigDecimal changePercentage;
    private LocalDate effectiveDate;
    private String reason;
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
