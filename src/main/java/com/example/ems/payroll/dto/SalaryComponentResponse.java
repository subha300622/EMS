package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.SalaryComponentType;

import java.time.LocalDateTime;

public class SalaryComponentResponse {

    private Long id;
    private Long organizationId;
    private String name;
    private String code;
    private String description;
    private SalaryComponentType componentType;
    private Boolean taxable;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalaryComponentResponse() {}

    public static SalaryComponentResponse fromEntity(SalaryComponent entity) {
        if (entity == null) {
            return null;
        }
        SalaryComponentResponse dto = new SalaryComponentResponse();
        dto.setId(entity.getId());
        dto.setOrganizationId(entity.getOrganizationId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setComponentType(entity.getComponentType());
        dto.setTaxable(entity.getTaxable());
        dto.setActive(entity.getActive());
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
