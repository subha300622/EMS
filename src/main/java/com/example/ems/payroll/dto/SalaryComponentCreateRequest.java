package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.SalaryComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SalaryComponentCreateRequest {

    @NotBlank(message = "Component name is required")
    @Size(max = 150, message = "Component name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Component code is required")
    @Size(max = 100, message = "Component code must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Component code must only contain letters, numbers, underscores, and hyphens")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Component type is required (EARNING, DEDUCTION, BENEFIT)")
    private SalaryComponentType componentType;

    private Boolean taxable = true;

    private Boolean active = true;

    public SalaryComponentCreateRequest() {}

    public SalaryComponentCreateRequest(String name, String code, String description, SalaryComponentType componentType, Boolean taxable, Boolean active) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.componentType = componentType;
        this.taxable = taxable != null ? taxable : true;
        this.active = active != null ? active : true;
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
}
