package com.example.ems.payroll.validation;

import com.example.ems.payroll.entity.SalaryStructureStatus;

import java.util.ArrayList;
import java.util.List;

public class SalaryValidationResult {

    private boolean valid;
    private Long structureId;
    private String structureCode;
    private SalaryStructureStatus status;
    private List<String> calculationOrder = new ArrayList<>();
    private List<SalaryValidationError> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public SalaryValidationResult() {}

    public SalaryValidationResult(boolean valid, Long structureId, String structureCode, SalaryStructureStatus status) {
        this.valid = valid;
        this.structureId = structureId;
        this.structureCode = structureCode;
        this.status = status;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Long getStructureId() {
        return structureId;
    }

    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    public String getStructureCode() {
        return structureCode;
    }

    public void setStructureCode(String structureCode) {
        this.structureCode = structureCode;
    }

    public SalaryStructureStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryStructureStatus status) {
        this.status = status;
    }

    public List<String> getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(List<String> calculationOrder) {
        this.calculationOrder = calculationOrder;
    }

    public List<SalaryValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<SalaryValidationError> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
