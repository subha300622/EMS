package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.CalculationBaseType;
import com.example.ems.payroll.entity.CalculationType;
import com.example.ems.payroll.entity.SalaryComponentType;
import com.example.ems.payroll.entity.SalaryStructureComponent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StructureComponentResponse {

    private Long id;
    private Long salaryStructureId;
    private Long componentId;
    private String componentName;
    private String componentCode;
    private SalaryComponentType componentType;
    private Boolean taxable;
    private CalculationType calculationType;
    private CalculationBaseType calculationBaseType;
    private Long calculationBaseComponentId;
    private String calculationBaseComponentName;
    private String calculationBaseComponentCode;
    private BigDecimal fixedAmount;
    private BigDecimal percentage;
    private String formula;
    private Integer calculationOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StructureComponentResponse() {}

    public static StructureComponentResponse fromEntity(SalaryStructureComponent entity) {
        if (entity == null) {
            return null;
        }
        StructureComponentResponse dto = new StructureComponentResponse();
        dto.setId(entity.getId());
        dto.setSalaryStructureId(entity.getSalaryStructure() != null ? entity.getSalaryStructure().getId() : null);

        if (entity.getSalaryComponent() != null) {
            dto.setComponentId(entity.getSalaryComponent().getId());
            dto.setComponentName(entity.getSalaryComponent().getName());
            dto.setComponentCode(entity.getSalaryComponent().getCode());
            dto.setComponentType(entity.getSalaryComponent().getComponentType());
            dto.setTaxable(entity.getSalaryComponent().getTaxable());
        }

        dto.setCalculationType(entity.getCalculationType());
        dto.setCalculationBaseType(entity.getCalculationBaseType());

        if (entity.getCalculationBaseComponent() != null) {
            dto.setCalculationBaseComponentId(entity.getCalculationBaseComponent().getId());
            dto.setCalculationBaseComponentName(entity.getCalculationBaseComponent().getName());
            dto.setCalculationBaseComponentCode(entity.getCalculationBaseComponent().getCode());
        }

        dto.setFixedAmount(entity.getFixedAmount());
        dto.setPercentage(entity.getPercentage());
        dto.setFormula(entity.getFormula());
        dto.setCalculationOrder(entity.getCalculationOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSalaryStructureId() {
        return salaryStructureId;
    }

    public void setSalaryStructureId(Long salaryStructureId) {
        this.salaryStructureId = salaryStructureId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public SalaryComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(SalaryComponentType componentType) {
        this.componentType = componentType;
    }

    public Boolean getTaxable() {
        return taxable;
    }

    public void setTaxable(Boolean taxable) {
        this.taxable = taxable;
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

    public String getCalculationBaseComponentName() {
        return calculationBaseComponentName;
    }

    public void setCalculationBaseComponentName(String calculationBaseComponentName) {
        this.calculationBaseComponentName = calculationBaseComponentName;
    }

    public String getCalculationBaseComponentCode() {
        return calculationBaseComponentCode;
    }

    public void setCalculationBaseComponentCode(String calculationBaseComponentCode) {
        this.calculationBaseComponentCode = calculationBaseComponentCode;
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
