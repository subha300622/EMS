package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayrollEmployee;
import com.example.ems.payroll.entity.PayrollEmployeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollEmployeeResponse {

    private Long id;
    private Long organizationId;
    private Long payrollRunId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal grossAmount;
    private BigDecimal benefitsAmount;
    private BigDecimal deductionsAmount;
    private BigDecimal netAmount;
    private String currency;
    private PayrollEmployeeStatus status;
    private LocalDate calculationDate;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PayrollEmployeeResponse() {}

    public static PayrollEmployeeResponse fromEntity(PayrollEmployee pe) {
        if (pe == null) return null;
        PayrollEmployeeResponse res = new PayrollEmployeeResponse();
        res.setId(pe.getId());
        res.setOrganizationId(pe.getOrganizationId());
        res.setPayrollRunId(pe.getPayrollRunId());
        res.setEmployeeId(pe.getEmployeeId());
        res.setEmployeeName(pe.getEmployeeName());
        res.setEmployeeCode(pe.getEmployeeCode());
        res.setGrossAmount(pe.getGrossAmount());
        res.setBenefitsAmount(pe.getBenefitsAmount());
        res.setDeductionsAmount(pe.getDeductionsAmount());
        res.setNetAmount(pe.getNetAmount());
        res.setCurrency(pe.getCurrency());
        res.setStatus(pe.getStatus());
        res.setCalculationDate(pe.getCalculationDate());
        res.setErrorMessage(pe.getErrorMessage());
        res.setCreatedAt(pe.getCreatedAt());
        res.setUpdatedAt(pe.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayrollEmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollEmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
