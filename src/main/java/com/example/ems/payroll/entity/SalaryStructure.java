package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "salary_structures")
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long employeeId;

    @Column(nullable = false)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal allowances = BigDecimal.ZERO;

    public SalaryStructure() {}

    public SalaryStructure(Long employeeId, BigDecimal basicSalary, BigDecimal hra, BigDecimal allowances) {
        this.employeeId = employeeId;
        this.basicSalary = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        this.hra = hra != null ? hra : BigDecimal.ZERO;
        this.allowances = allowances != null ? allowances : BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
