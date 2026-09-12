package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PayslipDetailResponse {

    private Long payrollRunId;
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
    private LocalDate calculationDate;
    private List<PayrollItemResponse> items;

    public PayslipDetailResponse() {}

    public PayslipDetailResponse(Long payrollRunId, Long payrollEmployeeId, Long employeeId,
                                 String employeeName, String employeeCode, LocalDate periodStart,
                                 LocalDate periodEnd, String currency, BigDecimal grossAmount,
                                 BigDecimal benefitsAmount, BigDecimal deductionsAmount,
                                 BigDecimal netAmount, String status, LocalDate calculationDate,
                                 List<PayrollItemResponse> items) {
        this.payrollRunId = payrollRunId;
        this.payrollEmployeeId = payrollEmployeeId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = currency;
        this.grossAmount = grossAmount;
        this.benefitsAmount = benefitsAmount;
        this.deductionsAmount = deductionsAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.calculationDate = calculationDate;
        this.items = items;
    }

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
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

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public List<PayrollItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PayrollItemResponse> items) {
        this.items = items;
    }
}
