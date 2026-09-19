package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_items")
public class PayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "payroll_employee_id", nullable = false)
    private Long payrollEmployeeId;

    @Column(name = "salary_component_id")
    private Long salaryComponentId;

    @Column(name = "component_code", nullable = false, length = 50)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Column(name = "component_type", nullable = false, length = 30)
    private String componentType; // EARNING, DEDUCTION, BENEFIT

    @Column(name = "calculation_type", nullable = false, length = 30)
    private String calculationType; // FIXED, PERCENTAGE, FORMULA

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "percentage", precision = 7, scale = 4)
    private BigDecimal percentage;

    @Column(name = "calculation_base", length = 50)
    private String calculationBase;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PayrollItem() {}

    public PayrollItem(Long organizationId, Long payrollEmployeeId, Long salaryComponentId,
                       String componentCode, String componentName, String componentType,
                       String calculationType, BigDecimal amount, BigDecimal percentage,
                       String calculationBase) {
        this.organizationId = organizationId;
        this.payrollEmployeeId = payrollEmployeeId;
        this.salaryComponentId = salaryComponentId;
        this.componentCode = componentCode;
        this.componentName = componentName;
        this.componentType = componentType;
        this.calculationType = calculationType;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.percentage = percentage;
        this.calculationBase = calculationBase;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.amount == null) this.amount = BigDecimal.ZERO;
    }

    // Getters and Setters

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

    public Long getPayrollEmployeeId() {
        return payrollEmployeeId;
    }

    public void setPayrollEmployeeId(Long payrollEmployeeId) {
        this.payrollEmployeeId = payrollEmployeeId;
    }

    public Long getSalaryComponentId() {
        return salaryComponentId;
    }

    public void setSalaryComponentId(Long salaryComponentId) {
        this.salaryComponentId = salaryComponentId;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getCalculationBase() {
        return calculationBase;
    }

    public void setCalculationBase(String calculationBase) {
        this.calculationBase = calculationBase;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
