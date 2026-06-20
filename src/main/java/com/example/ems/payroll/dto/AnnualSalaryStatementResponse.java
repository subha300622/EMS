package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AnnualSalaryStatementResponse {

    @Schema(example = "string")
    private String financialYear;
    private SalarySummaryInfo salarySummary;
    @Schema(example = "2026-06-19T10:00:00")
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
        @Schema(example = "120000.00")
        private BigDecimal totalGrossSalary;
        @Schema(example = "5000.00")
        private BigDecimal totalDeductions;
        @Schema(example = "120000.00")
        private BigDecimal totalNetSalary;
        @Schema(example = "6")
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
