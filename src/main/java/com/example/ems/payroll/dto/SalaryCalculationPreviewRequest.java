package com.example.ems.payroll.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalaryCalculationPreviewRequest {

    private LocalDate effectiveDate;

    private List<EmployeeSalaryComponentValueRequest> overrideValues = new ArrayList<>();

    public SalaryCalculationPreviewRequest() {}

    public SalaryCalculationPreviewRequest(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public SalaryCalculationPreviewRequest(LocalDate effectiveDate, List<EmployeeSalaryComponentValueRequest> overrideValues) {
        this.effectiveDate = effectiveDate;
        this.overrideValues = overrideValues != null ? overrideValues : new ArrayList<>();
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public List<EmployeeSalaryComponentValueRequest> getOverrideValues() {
        return overrideValues;
    }

    public void setOverrideValues(List<EmployeeSalaryComponentValueRequest> overrideValues) {
        this.overrideValues = overrideValues;
    }
}
