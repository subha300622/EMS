package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AnnualSalaryStatementResponse {

    private String financialYear;
    private SalarySummaryInfo salarySummary;
    private LocalDateTime generatedAt;

    public AnnualSalaryStatementResponse() {}

    public AnnualSalaryStatementResponse(String financialYear, SalarySummaryInfo salarySummary, LocalDateTime generatedAt) {
        this.financialYear = financialYear;
        this.salarySummary = salarySummary;
        this.generatedAt = generatedAt;
    }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public SalarySummaryInfo getSalarySummary() { return salarySummary; }
    public void setSalarySummary(SalarySummaryInfo salarySummary) { this.salarySummary = salarySummary; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public static class SalarySummaryInfo {
        private BigDecimal totalGrossSalary;
        private BigDecimal totalDeductions;
        private BigDecimal totalNetSalary;
        private int monthsProcessed;

        public SalarySummaryInfo() {}

        public SalarySummaryInfo(BigDecimal totalGrossSalary, BigDecimal totalDeductions, BigDecimal totalNetSalary, int monthsProcessed) {
            this.totalGrossSalary = totalGrossSalary;
            this.totalDeductions = totalDeductions;
            this.totalNetSalary = totalNetSalary;
            this.monthsProcessed = monthsProcessed;
        }

        public BigDecimal getTotalGrossSalary() { return totalGrossSalary; }
        public void setTotalGrossSalary(BigDecimal totalGrossSalary) { this.totalGrossSalary = totalGrossSalary; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getTotalNetSalary() { return totalNetSalary; }
        public void setTotalNetSalary(BigDecimal totalNetSalary) { this.totalNetSalary = totalNetSalary; }

        public int getMonthsProcessed() { return monthsProcessed; }
        public void setMonthsProcessed(int monthsProcessed) { this.monthsProcessed = monthsProcessed; }
    }
}
