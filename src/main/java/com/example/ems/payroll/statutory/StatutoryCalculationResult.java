package com.example.ems.payroll.statutory;

import java.math.BigDecimal;

public class StatutoryCalculationResult {

    // Employee Deductions (Reduce Net Pay)
    private BigDecimal pfEmployeeDeduction = BigDecimal.ZERO;
    private BigDecimal esiEmployeeDeduction = BigDecimal.ZERO;
    private BigDecimal ptEmployeeDeduction = BigDecimal.ZERO;
    private BigDecimal tdsEmployeeDeduction = BigDecimal.ZERO;
    private BigDecimal totalEmployeeDeductions = BigDecimal.ZERO;

    // Employer Contributions (Employer Payroll Cost - NEVER reduces Employee Net Pay)
    private BigDecimal employerEpfContribution = BigDecimal.ZERO;
    private BigDecimal employerEpsContribution = BigDecimal.ZERO;
    private BigDecimal employerEsiContribution = BigDecimal.ZERO;
    private BigDecimal totalEmployerCost = BigDecimal.ZERO;

    // Detailed Breakdown Objects
    private PfCalculationResult pfResult;
    private EsiCalculationResult esiResult;
    private PtCalculationResult ptResult;
    private TdsCalculationResult tdsResult;

    public StatutoryCalculationResult() {}

    public StatutoryCalculationResult(PfCalculationResult pfResult, EsiCalculationResult esiResult,
                                      PtCalculationResult ptResult, TdsCalculationResult tdsResult) {
        this.pfResult = pfResult;
        this.esiResult = esiResult;
        this.ptResult = ptResult;
        this.tdsResult = tdsResult;

        if (pfResult != null) {
            this.pfEmployeeDeduction = pfResult.getEmployeeContribution();
            this.employerEpfContribution = pfResult.getEmployerEpfContribution();
            this.employerEpsContribution = pfResult.getEmployerEpsContribution();
        }
        if (esiResult != null && esiResult.isApplicable()) {
            this.esiEmployeeDeduction = esiResult.getEmployeeContribution();
            this.employerEsiContribution = esiResult.getEmployerContribution();
        }
        if (ptResult != null && ptResult.isApplicable()) {
            this.ptEmployeeDeduction = ptResult.getPtAmount();
        }
        if (tdsResult != null && tdsResult.isApplicable()) {
            this.tdsEmployeeDeduction = tdsResult.getMonthlyTds();
        }

        this.totalEmployeeDeductions = this.pfEmployeeDeduction
                .add(this.esiEmployeeDeduction)
                .add(this.ptEmployeeDeduction)
                .add(this.tdsEmployeeDeduction);

        this.totalEmployerCost = this.employerEpfContribution
                .add(this.employerEpsContribution)
                .add(this.employerEsiContribution);
    }

    public BigDecimal getPfEmployeeDeduction() {
        return pfEmployeeDeduction;
    }

    public void setPfEmployeeDeduction(BigDecimal pfEmployeeDeduction) {
        this.pfEmployeeDeduction = pfEmployeeDeduction;
    }

    public BigDecimal getEsiEmployeeDeduction() {
        return esiEmployeeDeduction;
    }

    public void setEsiEmployeeDeduction(BigDecimal esiEmployeeDeduction) {
        this.esiEmployeeDeduction = esiEmployeeDeduction;
    }

    public BigDecimal getPtEmployeeDeduction() {
        return ptEmployeeDeduction;
    }

    public void setPtEmployeeDeduction(BigDecimal ptEmployeeDeduction) {
        this.ptEmployeeDeduction = ptEmployeeDeduction;
    }

    public BigDecimal getTdsEmployeeDeduction() {
        return tdsEmployeeDeduction;
    }

    public void setTdsEmployeeDeduction(BigDecimal tdsEmployeeDeduction) {
        this.tdsEmployeeDeduction = tdsEmployeeDeduction;
    }

    public BigDecimal getTotalEmployeeDeductions() {
        return totalEmployeeDeductions;
    }

    public void setTotalEmployeeDeductions(BigDecimal totalEmployeeDeductions) {
        this.totalEmployeeDeductions = totalEmployeeDeductions;
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

    public BigDecimal getEmployerEsiContribution() {
        return employerEsiContribution;
    }

    public void setEmployerEsiContribution(BigDecimal employerEsiContribution) {
        this.employerEsiContribution = employerEsiContribution;
    }

    public BigDecimal getTotalEmployerCost() {
        return totalEmployerCost;
    }

    public void setTotalEmployerCost(BigDecimal totalEmployerCost) {
        this.totalEmployerCost = totalEmployerCost;
    }

    public PfCalculationResult getPfResult() {
        return pfResult;
    }

    public void setPfResult(PfCalculationResult pfResult) {
        this.pfResult = pfResult;
    }

    public EsiCalculationResult getEsiResult() {
        return esiResult;
    }

    public void setEsiResult(EsiCalculationResult esiResult) {
        this.esiResult = esiResult;
    }

    public PtCalculationResult getPtResult() {
        return ptResult;
    }

    public void setPtResult(PtCalculationResult ptResult) {
        this.ptResult = ptResult;
    }

    public TdsCalculationResult getTdsResult() {
        return tdsResult;
    }

    public void setTdsResult(TdsCalculationResult tdsResult) {
        this.tdsResult = tdsResult;
    }
}
