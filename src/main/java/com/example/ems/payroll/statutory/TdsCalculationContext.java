package com.example.ems.payroll.statutory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TdsCalculationContext {

    private Long employeeId;
    private Long organizationId;
    private String taxRegime = "NEW"; // NEW, OLD
    private LocalDate payrollDate = LocalDate.now();
    private BigDecimal currentMonthGross = BigDecimal.ZERO;
    private BigDecimal ytdTaxableIncome = BigDecimal.ZERO;
    private BigDecimal ytdTdsDeducted = BigDecimal.ZERO;
    private BigDecimal declaredOtherIncome = BigDecimal.ZERO;
    private BigDecimal chapterViaDeductions = BigDecimal.ZERO; // 80C, 80D (applicable in OLD regime)

    public TdsCalculationContext() {}

    public TdsCalculationContext(Long employeeId, Long organizationId, String taxRegime,
                                 LocalDate payrollDate, BigDecimal currentMonthGross,
                                 BigDecimal ytdTaxableIncome, BigDecimal ytdTdsDeducted) {
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.taxRegime = taxRegime != null ? taxRegime : "NEW";
        this.payrollDate = payrollDate != null ? payrollDate : LocalDate.now();
        this.currentMonthGross = currentMonthGross != null ? currentMonthGross : BigDecimal.ZERO;
        this.ytdTaxableIncome = ytdTaxableIncome != null ? ytdTaxableIncome : BigDecimal.ZERO;
        this.ytdTdsDeducted = ytdTdsDeducted != null ? ytdTdsDeducted : BigDecimal.ZERO;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }

    public LocalDate getPayrollDate() {
        return payrollDate;
    }

    public void setPayrollDate(LocalDate payrollDate) {
        this.payrollDate = payrollDate;
    }

    public BigDecimal getCurrentMonthGross() {
        return currentMonthGross;
    }

    public void setCurrentMonthGross(BigDecimal currentMonthGross) {
        this.currentMonthGross = currentMonthGross;
    }

    public BigDecimal getYtdTaxableIncome() {
        return ytdTaxableIncome;
    }

    public void setYtdTaxableIncome(BigDecimal ytdTaxableIncome) {
        this.ytdTaxableIncome = ytdTaxableIncome;
    }

    public BigDecimal getYtdTdsDeducted() {
        return ytdTdsDeducted;
    }

    public void setYtdTdsDeducted(BigDecimal ytdTdsDeducted) {
        this.ytdTdsDeducted = ytdTdsDeducted;
    }

    public BigDecimal getDeclaredOtherIncome() {
        return declaredOtherIncome;
    }

    public void setDeclaredOtherIncome(BigDecimal declaredOtherIncome) {
        this.declaredOtherIncome = declaredOtherIncome;
    }

    public BigDecimal getChapterViaDeductions() {
        return chapterViaDeductions;
    }

    public void setChapterViaDeductions(BigDecimal chapterViaDeductions) {
        this.chapterViaDeductions = chapterViaDeductions;
    }
}
