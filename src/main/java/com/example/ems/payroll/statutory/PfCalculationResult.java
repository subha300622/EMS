package com.example.ems.payroll.statutory;

import java.math.BigDecimal;

public class PfCalculationResult {

    private boolean applicable = true;
    private BigDecimal wageBase = BigDecimal.ZERO;
    private BigDecimal employeeContribution = BigDecimal.ZERO;
    private BigDecimal employerEpfContribution = BigDecimal.ZERO;
    private BigDecimal employerEpsContribution = BigDecimal.ZERO;
    private BigDecimal totalEmployerContribution = BigDecimal.ZERO;
    private String calculationType = "STATUTORY_CAPPED";

    public PfCalculationResult() {}

    public PfCalculationResult(boolean applicable, BigDecimal wageBase, BigDecimal employeeContribution,
                               BigDecimal employerEpfContribution, BigDecimal employerEpsContribution,
                               BigDecimal totalEmployerContribution, String calculationType) {
        this.applicable = applicable;
        this.wageBase = wageBase != null ? wageBase : BigDecimal.ZERO;
        this.employeeContribution = employeeContribution != null ? employeeContribution : BigDecimal.ZERO;
        this.employerEpfContribution = employerEpfContribution != null ? employerEpfContribution : BigDecimal.ZERO;
        this.employerEpsContribution = employerEpsContribution != null ? employerEpsContribution : BigDecimal.ZERO;
        this.totalEmployerContribution = totalEmployerContribution != null ? totalEmployerContribution : BigDecimal.ZERO;
        this.calculationType = calculationType;
    }

    public boolean isApplicable() {
        return applicable;
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
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

    public BigDecimal getEmployerEpfContribution() {
        return employerEpfContribution;
    }

    public void setEmployerEpfContribution(BigDecimal employerEpfContribution) {
        this.employerEpfContribution = employerEpfContribution;
    }

    public BigDecimal getEmployerEpsContribution() {
        return employerEpsContribution;
    }

    public void setEmployerEpsContribution(BigDecimal employerEpsContribution) {
        this.employerEpsContribution = employerEpsContribution;
    }

    public BigDecimal getTotalEmployerContribution() {
        return totalEmployerContribution;
    }

    public void setTotalEmployerContribution(BigDecimal totalEmployerContribution) {
        this.totalEmployerContribution = totalEmployerContribution;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }
}
