package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.CalculationBaseType;
import com.example.ems.payroll.entity.CalculationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class StructureComponentCreateRequest {

    @NotNull(message = "Salary component ID is required")
    private Long componentId;

    @NotNull(message = "Calculation type is required (FIXED, PERCENTAGE, FORMULA)")
    private CalculationType calculationType;

    private CalculationBaseType calculationBaseType = CalculationBaseType.NONE;

    private Long calculationBaseComponentId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fixed amount must be non-negative")
    private BigDecimal fixedAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Percentage must be non-negative")
    private BigDecimal percentage;

    @Size(max = 1000, message = "Formula must not exceed 1000 characters")
    private String formula;

    @Min(value = 1, message = "Calculation order must be at least 1")
    private Integer calculationOrder = 1;

    public StructureComponentCreateRequest() {}

    public StructureComponentCreateRequest(Long componentId, CalculationType calculationType, CalculationBaseType calculationBaseType, Long calculationBaseComponentId, BigDecimal fixedAmount, BigDecimal percentage, String formula, Integer calculationOrder) {
        this.componentId = componentId;
        this.calculationType = calculationType;
        this.calculationBaseType = calculationBaseType != null ? calculationBaseType : CalculationBaseType.NONE;
        this.calculationBaseComponentId = calculationBaseComponentId;
        this.fixedAmount = fixedAmount;
        this.percentage = percentage;
        this.formula = formula;
        this.calculationOrder = calculationOrder != null ? calculationOrder : 1;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public CalculationBaseType getCalculationBaseType() {
        return calculationBaseType;
    }

    public void setCalculationBaseType(CalculationBaseType calculationBaseType) {
        this.calculationBaseType = calculationBaseType;
    }

    public Long getCalculationBaseComponentId() {
        return calculationBaseComponentId;
    }

    public void setCalculationBaseComponentId(Long calculationBaseComponentId) {
        this.calculationBaseComponentId = calculationBaseComponentId;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Integer getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(Integer calculationOrder) {
        this.calculationOrder = calculationOrder;
    }
}
