package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayrollEmployee;
import com.example.ems.payroll.entity.PayrollRun;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeePayrollHistoryResponse {

    private Long runId;
    private Long payrollEmployeeId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String currency;
    private BigDecimal grossAmount;
    private BigDecimal benefitsAmount;
    private BigDecimal deductionsAmount;
    private BigDecimal netAmount;
    private String status;
    private String runStatus;
    private LocalDate calculationDate;
    private LocalDateTime approvedAt;
    private LocalDateTime finalizedAt;
    private LocalDateTime createdAt;

    public EmployeePayrollHistoryResponse() {}

    public static EmployeePayrollHistoryResponse from(PayrollEmployee pe, PayrollRun run) {
        if (pe == null) return null;
        EmployeePayrollHistoryResponse res = new EmployeePayrollHistoryResponse();
        res.setPayrollEmployeeId(pe.getId());
        res.setEmployeeId(pe.getEmployeeId());
        res.setEmployeeName(pe.getEmployeeName());
        res.setEmployeeCode(pe.getEmployeeCode());
        res.setGrossAmount(pe.getGrossAmount());
        res.setBenefitsAmount(pe.getBenefitsAmount());
        res.setDeductionsAmount(pe.getDeductionsAmount());
        res.setNetAmount(pe.getNetAmount());
        res.setCurrency(pe.getCurrency());
        res.setStatus(pe.getStatus() != null ? pe.getStatus().name() : null);
        res.setCalculationDate(pe.getCalculationDate());
        res.setCreatedAt(pe.getCreatedAt());

        if (run != null) {
            res.setRunId(run.getId());
            res.setPeriodStart(run.getPeriodStart());
            res.setPeriodEnd(run.getPeriodEnd());
            res.setRunStatus(run.getStatus() != null ? run.getStatus().name() : null);
            res.setApprovedAt(run.getApprovedAt());
            res.setFinalizedAt(run.getFinalizedAt());
        } else {
            res.setRunId(pe.getPayrollRunId());
        }

        return res;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getPayrollEmployeeId() {
        return payrollEmployeeId;
    }

    public void setPayrollEmployeeId(Long payrollEmployeeId) {
        this.payrollEmployeeId = payrollEmployeeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getBenefitsAmount() {
        return benefitsAmount;
    }

    public void setBenefitsAmount(BigDecimal benefitsAmount) {
        this.benefitsAmount = benefitsAmount;
    }

    public BigDecimal getDeductionsAmount() {
        return deductionsAmount;
    }

    public void setDeductionsAmount(BigDecimal deductionsAmount) {
        this.deductionsAmount = deductionsAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
