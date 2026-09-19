package com.example.ems.payroll.statutory;

import java.math.BigDecimal;

public class TdsCalculationResult {

    private boolean applicable = true;
    private String financialYear = "2026-27";
    private String taxRegime = "NEW";
    private BigDecimal monthlyTds = BigDecimal.ZERO;
    private BigDecimal projectedAnnualGross = BigDecimal.ZERO;
    private BigDecimal taxableIncome = BigDecimal.ZERO;
    private BigDecimal standardDeduction = BigDecimal.ZERO;
    private BigDecimal slabTax = BigDecimal.ZERO;
    private BigDecimal section87aRebate = BigDecimal.ZERO;
    private BigDecimal healthAndEducationCess = BigDecimal.ZERO;
    private BigDecimal totalProjectedAnnualTax = BigDecimal.ZERO;
    private BigDecimal ytdTdsDeducted = BigDecimal.ZERO;
    private int remainingPeriods = 12;

    public TdsCalculationResult() {}

    public boolean isApplicable() {
        return applicable;
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public String getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }

    public BigDecimal getMonthlyTds() {
        return monthlyTds;
    }

    public void setMonthlyTds(BigDecimal monthlyTds) {
        this.monthlyTds = monthlyTds;
    }

    public BigDecimal getProjectedAnnualGross() {
        return projectedAnnualGross;
    }

    public void setProjectedAnnualGross(BigDecimal projectedAnnualGross) {
        this.projectedAnnualGross = projectedAnnualGross;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getStandardDeduction() {
        return standardDeduction;
    }

    public void setStandardDeduction(BigDecimal standardDeduction) {
        this.standardDeduction = standardDeduction;
    }

    public BigDecimal getSlabTax() {
        return slabTax;
    }

    public void setSlabTax(BigDecimal slabTax) {
        this.slabTax = slabTax;
    }

    public BigDecimal getSection87aRebate() {
        return section87aRebate;
    }

    public void setSection87aRebate(BigDecimal section87aRebate) {
        this.section87aRebate = section87aRebate;
    }

    public BigDecimal getHealthAndEducationCess() {
        return healthAndEducationCess;
    }

    public void setHealthAndEducationCess(BigDecimal healthAndEducationCess) {
        this.healthAndEducationCess = healthAndEducationCess;
    }

    public BigDecimal getTotalProjectedAnnualTax() {
        return totalProjectedAnnualTax;
    }

    public void setTotalProjectedAnnualTax(BigDecimal totalProjectedAnnualTax) {
        this.totalProjectedAnnualTax = totalProjectedAnnualTax;
    }

    public BigDecimal getYtdTdsDeducted() {
        return ytdTdsDeducted;
    }

    public void setYtdTdsDeducted(BigDecimal ytdTdsDeducted) {
        this.ytdTdsDeducted = ytdTdsDeducted;
    }

    public int getRemainingPeriods() {
        return remainingPeriods;
    }

    public void setRemainingPeriods(int remainingPeriods) {
        this.remainingPeriods = remainingPeriods;
    }
}
