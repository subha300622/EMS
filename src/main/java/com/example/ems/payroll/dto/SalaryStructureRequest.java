package com.example.ems.payroll.dto;

import java.math.BigDecimal;

public class SalaryStructureRequest {

    private Long employeeId;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowances;

    public SalaryStructureRequest() {}

    public SalaryStructureRequest(Long employeeId, BigDecimal basicSalary, BigDecimal hra, BigDecimal allowances) {
        this.employeeId = employeeId;
        this.basicSalary = basicSalary;
        this.hra = hra;
        this.allowances = allowances;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }
}
