package com.example.ems.appraisal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppraisalIncrementResponseDto {
    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private BigDecimal currentSalary;
    private Double incrementPercentage;
    private BigDecimal incrementAmount;
    private Double bonusPercentage;
    private BigDecimal bonusAmount;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String status; // PROPOSED, APPROVED, APPLIED, REJECTED
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String appliedByName;
    private LocalDateTime appliedAt;
    private String remarks;
    private LocalDateTime createdAt;

    public AppraisalIncrementResponseDto() {}

    public AppraisalIncrementResponseDto(Long id, Long appraisalId, Long employeeId, String employeeName,
                                        BigDecimal currentSalary, Double incrementPercentage,
                                        BigDecimal incrementAmount, Double bonusPercentage,
                                        BigDecimal bonusAmount, BigDecimal newSalary,
                                        LocalDate effectiveDate, String status, String approvedByName,
                                        LocalDateTime approvedAt, String appliedByName,
                                        LocalDateTime appliedAt, String remarks, LocalDateTime createdAt) {
        this.id = id;
        this.appraisalId = appraisalId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.currentSalary = currentSalary;
        this.incrementPercentage = incrementPercentage;
        this.incrementAmount = incrementAmount;
        this.bonusPercentage = bonusPercentage;
        this.bonusAmount = bonusAmount;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.approvedByName = approvedByName;
        this.approvedAt = approvedAt;
        this.appliedByName = appliedByName;
        this.appliedAt = appliedAt;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public Double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(Double bonusPercentage) { this.bonusPercentage = bonusPercentage; }

    public BigDecimal getBonusAmount() { return bonusAmount; }
    public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getAppliedByName() { return appliedByName; }
    public void setAppliedByName(String appliedByName) { this.appliedByName = appliedByName; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
