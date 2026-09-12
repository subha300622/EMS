package com.example.ems.payroll.statutory;

import java.math.BigDecimal;

public class EsiCalculationResult {

    private boolean applicable = false;
    private String reason;
    private BigDecimal wageBase = BigDecimal.ZERO;
    private BigDecimal employeeContribution = BigDecimal.ZERO;
    private BigDecimal employerContribution = BigDecimal.ZERO;
    private boolean lowWageExempt = false;

    public EsiCalculationResult() {}

    public EsiCalculationResult(boolean applicable, String reason, BigDecimal wageBase,
                                BigDecimal employeeContribution, BigDecimal employerContribution,
                                boolean lowWageExempt) {
        this.applicable = applicable;
        this.reason = reason;
        this.wageBase = wageBase != null ? wageBase : BigDecimal.ZERO;
        this.employeeContribution = employeeContribution != null ? employeeContribution : BigDecimal.ZERO;
        this.employerContribution = employerContribution != null ? employerContribution : BigDecimal.ZERO;
        this.lowWageExempt = lowWageExempt;
    }

    public boolean isApplicable() {
        return applicable;
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getWageBase() {
        return wageBase;
    }

    public void setWageBase(BigDecimal wageBase) {
        this.wageBase = wageBase;
    }

    public BigDecimal getEmployeeContribution() {
        return employeeContribution;
    }

    public void setEmployeeContribution(BigDecimal employeeContribution) {
        this.employeeContribution = employeeContribution;
    }

    public BigDecimal getEmployerContribution() {
        return employerContribution;
    }

    public void setEmployerContribution(BigDecimal employerContribution) {
        this.employerContribution = employerContribution;
    }

    public boolean isLowWageExempt() {
        return lowWageExempt;
    }

    public void setLowWageExempt(boolean lowWageExempt) {
        this.lowWageExempt = lowWageExempt;
    }
}
