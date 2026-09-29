package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.CalculationType;
import com.example.ems.payroll.entity.SalaryComponentType;

import java.math.BigDecimal;

public class SalaryCalculatedComponentResponse {

    private Long componentId;
    private String componentCode;
    private String componentName;
    private SalaryComponentType componentType;
    private CalculationType calculationType;
    private BigDecimal appliedRate;
    private BigDecimal amount;
    private Boolean taxable;
    private Boolean overrideApplied;

    public SalaryCalculatedComponentResponse() {}

    public SalaryCalculatedComponentResponse(Long componentId, String componentCode, String componentName,
                                            SalaryComponentType componentType, CalculationType calculationType,
                                            BigDecimal appliedRate, BigDecimal amount, Boolean taxable, Boolean overrideApplied) {
        this.componentId = componentId;
        this.componentCode = componentCode;
        this.componentName = componentName;
        this.componentType = componentType;
        this.calculationType = calculationType;
        this.appliedRate = appliedRate;
        this.amount = amount;
        this.taxable = taxable;
        this.overrideApplied = overrideApplied;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
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

    public SalaryComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(SalaryComponentType componentType) {
        this.componentType = componentType;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public void setAppliedRate(BigDecimal appliedRate) {
        this.appliedRate = appliedRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getTaxable() {
        return taxable;
    }

    public void setTaxable(Boolean taxable) {
        this.taxable = taxable;
    }

    public Boolean getOverrideApplied() {
        return overrideApplied;
    }

    public void setOverrideApplied(Boolean overrideApplied) {
        this.overrideApplied = overrideApplied;
    }
}
