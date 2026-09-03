package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayrollItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollItemResponse {

    private Long id;
    private Long organizationId;
    private Long payrollEmployeeId;
    private Long salaryComponentId;
    private String componentCode;
    private String componentName;
    private String componentType;
    private String calculationType;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String calculationBase;
    private LocalDateTime createdAt;

    public PayrollItemResponse() {}

    public static PayrollItemResponse fromEntity(PayrollItem item) {
        if (item == null) return null;
        PayrollItemResponse res = new PayrollItemResponse();
        res.setId(item.getId());
        res.setOrganizationId(item.getOrganizationId());
        res.setPayrollEmployeeId(item.getPayrollEmployeeId());
        res.setSalaryComponentId(item.getSalaryComponentId());
        res.setComponentCode(item.getComponentCode());
        res.setComponentName(item.getComponentName());
        res.setComponentType(item.getComponentType());
        res.setCalculationType(item.getCalculationType());
        res.setAmount(item.getAmount());
        res.setPercentage(item.getPercentage());
        res.setCalculationBase(item.getCalculationBase());
        res.setCreatedAt(item.getCreatedAt());
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
