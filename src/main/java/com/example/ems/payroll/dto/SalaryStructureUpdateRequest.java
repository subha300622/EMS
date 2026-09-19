package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayFrequency;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SalaryStructureUpdateRequest {

    @Size(max = 150, message = "Salary structure name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    private PayFrequency payFrequency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    public SalaryStructureUpdateRequest() {}

    public SalaryStructureUpdateRequest(String name, String description, String currency, PayFrequency payFrequency, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.payFrequency = payFrequency;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
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
