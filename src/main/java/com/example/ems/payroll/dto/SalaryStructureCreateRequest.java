package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayFrequency;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SalaryStructureCreateRequest {

    @NotBlank(message = "Salary structure name is required")
    @Size(max = 150, message = "Salary structure name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Salary structure code is required")
    @Size(max = 100, message = "Salary structure code must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Salary structure code must only contain letters, numbers, underscores, and hyphens")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency = "INR";

    private PayFrequency payFrequency = PayFrequency.MONTHLY;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    public SalaryStructureCreateRequest() {}

    public SalaryStructureCreateRequest(String name, String code, String description, String currency, PayFrequency payFrequency, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.currency = currency != null ? currency : "INR";
        this.payFrequency = payFrequency != null ? payFrequency : PayFrequency.MONTHLY;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayFrequency getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(PayFrequency payFrequency) {
        this.payFrequency = payFrequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}
