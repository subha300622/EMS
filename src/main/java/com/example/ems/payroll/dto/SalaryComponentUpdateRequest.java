package com.example.ems.payroll.dto;

import jakarta.validation.constraints.Size;

public class SalaryComponentUpdateRequest {

    @Size(max = 150, message = "Component name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Boolean taxable;

    private Boolean active;

    public SalaryComponentUpdateRequest() {}

    public SalaryComponentUpdateRequest(String name, String description, Boolean taxable, Boolean active) {
        this.name = name;
        this.description = description;
        this.taxable = taxable;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
