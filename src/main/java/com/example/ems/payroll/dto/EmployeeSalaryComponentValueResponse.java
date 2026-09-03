package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.ComponentOverrideType;
import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import com.example.ems.payroll.entity.SalaryComponentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmployeeSalaryComponentValueResponse {

    private Long id;
    private Long salaryAssignmentId;
    private Long salaryComponentId;
    private String componentName;
    private String componentCode;
    private SalaryComponentType componentType;
    private ComponentOverrideType overrideType;
    private BigDecimal amount;
    private BigDecimal percentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeSalaryComponentValueResponse() {}

    public static EmployeeSalaryComponentValueResponse fromEntity(EmployeeSalaryComponentValue entity) {
        if (entity == null) {
            return null;
        }
        EmployeeSalaryComponentValueResponse dto = new EmployeeSalaryComponentValueResponse();
        dto.setId(entity.getId());
        dto.setSalaryAssignmentId(entity.getSalaryAssignment() != null ? entity.getSalaryAssignment().getId() : null);

        if (entity.getSalaryComponent() != null) {
            dto.setSalaryComponentId(entity.getSalaryComponent().getId());
            dto.setComponentName(entity.getSalaryComponent().getName());
            dto.setComponentCode(entity.getSalaryComponent().getCode());
            dto.setComponentType(entity.getSalaryComponent().getComponentType());
        }

        dto.setOverrideType(entity.getOverrideType());
        dto.setAmount(entity.getAmount());
        dto.setPercentage(entity.getPercentage());
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

    public Long getSalaryAssignmentId() {
        return salaryAssignmentId;
    }

    public void setSalaryAssignmentId(Long salaryAssignmentId) {
        this.salaryAssignmentId = salaryAssignmentId;
    }

    public Long getSalaryComponentId() {
        return salaryComponentId;
    }

    public void setSalaryComponentId(Long salaryComponentId) {
        this.salaryComponentId = salaryComponentId;
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
