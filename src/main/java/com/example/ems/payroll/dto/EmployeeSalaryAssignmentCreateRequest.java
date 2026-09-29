package com.example.ems.payroll.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSalaryAssignmentCreateRequest {

    @NotNull(message = "Salary structure ID is required")
    private Long salaryStructureId;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String reason;

    private List<EmployeeSalaryComponentValueRequest> componentValues = new ArrayList<>();

    public EmployeeSalaryAssignmentCreateRequest() {}

    public EmployeeSalaryAssignmentCreateRequest(Long salaryStructureId, LocalDate effectiveFrom, LocalDate effectiveTo, String reason) {
        this.salaryStructureId = salaryStructureId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.reason = reason;
    }

    public Long getSalaryStructureId() {
        return salaryStructureId;
    }

    public void setSalaryStructureId(Long salaryStructureId) {
        this.salaryStructureId = salaryStructureId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<EmployeeSalaryComponentValueRequest> getComponentValues() {
        return componentValues;
    }

    public void setComponentValues(List<EmployeeSalaryComponentValueRequest> componentValues) {
        this.componentValues = componentValues;
    }
}
