package com.example.ems.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveTypeRequest {

    @NotBlank(message = "Leave type name is required")
    private String name;

    private String description;

    @NotNull(message = "Default days are required")
    @Min(value = 0, message = "Default days cannot be negative")
    private Integer defaultDays;

    public LeaveTypeRequest() {}

    public LeaveTypeRequest(String name, String description, Integer defaultDays) {
        this.name = name;
        this.description = description;
        this.defaultDays = defaultDays;
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

    public Integer getDefaultDays() {
        return defaultDays;
    }

    public void setDefaultDays(Integer defaultDays) {
        this.defaultDays = defaultDays;
    }
}
