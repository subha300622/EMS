package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalaryCalculationResponse {

    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long assignmentId;
    private Long salaryStructureId;
    private String salaryStructureName;
    private String salaryStructureCode;
    private Integer salaryStructureVersion;
    private String currency;
    private LocalDate effectiveDate;
    private List<SalaryCalculatedComponentResponse> components = new ArrayList<>();
    private BigDecimal grossPay = BigDecimal.ZERO;
    private BigDecimal totalBenefits = BigDecimal.ZERO;
    private BigDecimal totalDeductions = BigDecimal.ZERO;
    private BigDecimal netPay = BigDecimal.ZERO;
    private LocalDateTime calculatedAt;

    public SalaryCalculationResponse() {
        this.calculatedAt = LocalDateTime.now();
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

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getSalaryStructureId() {
        return salaryStructureId;
    }

    public void setSalaryStructureId(Long salaryStructureId) {
        this.salaryStructureId = salaryStructureId;
    }

    public String getSalaryStructureName() {
        return salaryStructureName;
    }

    public void setSalaryStructureName(String salaryStructureName) {
        this.salaryStructureName = salaryStructureName;
    }

    public String getSalaryStructureCode() {
        return salaryStructureCode;
    }

    public void setSalaryStructureCode(String salaryStructureCode) {
        this.salaryStructureCode = salaryStructureCode;
    }

    public Integer getSalaryStructureVersion() {
        return salaryStructureVersion;
    }

    public void setSalaryStructureVersion(Integer salaryStructureVersion) {
        this.salaryStructureVersion = salaryStructureVersion;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public List<SalaryCalculatedComponentResponse> getComponents() {
        return components;
    }

    public void setComponents(List<SalaryCalculatedComponentResponse> components) {
        this.components = components;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }

    public BigDecimal getTotalBenefits() {
        return totalBenefits;
    }

    public void setTotalBenefits(BigDecimal totalBenefits) {
        this.totalBenefits = totalBenefits;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
