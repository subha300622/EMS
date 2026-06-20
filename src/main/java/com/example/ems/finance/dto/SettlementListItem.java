package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementListItem {
    private Long settlementId;
    private Long employeeId;
    private String employeeName;
    private String department;
    private LocalDate lastWorkingDate;
    private BigDecimal grossAmount;
    private BigDecimal deductionAmount;
    private BigDecimal netAmount;
    private String status;

    public SettlementListItem() {}

    public SettlementListItem(Long settlementId, Long employeeId, String employeeName, String department, LocalDate lastWorkingDate, BigDecimal grossAmount, BigDecimal deductionAmount, BigDecimal netAmount, String status) {
        this.settlementId = settlementId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.lastWorkingDate = lastWorkingDate;
        this.grossAmount = grossAmount;
        this.deductionAmount = deductionAmount;
        this.netAmount = netAmount;
        this.status = status;
    }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
