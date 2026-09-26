package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.ComponentOverrideType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EmployeeSalaryComponentValueRequest {

    @NotNull(message = "Salary component ID is required")
    private Long salaryComponentId;

    @NotNull(message = "Override type is required")
    private ComponentOverrideType overrideType = ComponentOverrideType.FIXED_AMOUNT;

    private BigDecimal amount;

    private BigDecimal percentage;

    public EmployeeSalaryComponentValueRequest() {}

    public EmployeeSalaryComponentValueRequest(Long salaryComponentId, ComponentOverrideType overrideType, BigDecimal amount, BigDecimal percentage) {
        this.salaryComponentId = salaryComponentId;
        this.overrideType = overrideType;
        this.amount = amount;
        this.percentage = percentage;
    }

    public Long getSalaryComponentId() {
        return salaryComponentId;
    }

    public void setSalaryComponentId(Long salaryComponentId) {
        this.salaryComponentId = salaryComponentId;
    }

    public ComponentOverrideType getOverrideType() {
        return overrideType;
    }

    public void setOverrideType(ComponentOverrideType overrideType) {
        this.overrideType = overrideType;
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
}
