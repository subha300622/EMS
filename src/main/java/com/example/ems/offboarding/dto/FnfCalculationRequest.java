package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(description = "Parameters for backend F&F Settlement calculation")
public class FnfCalculationRequest {

    @PositiveOrZero
    @Schema(description = "Days worked in the exit month", example = "17")
    private Integer salaryDaysWorked = 0;

    @PositiveOrZero
    @Schema(description = "Pending/unpaid salary from prior months", example = "0")
    private BigDecimal unpaidSalary = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Encashment of accrued leaves", example = "12500")
    private BigDecimal leaveEncashment = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Applicable bonus payout", example = "10000")
    private BigDecimal bonus = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Applicable performance incentives", example = "5000")
    private BigDecimal incentives = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Overtime pay", example = "2000")
    private BigDecimal overtime = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Pending expense reimbursements", example = "3500")
    private BigDecimal reimbursements = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Gratuity amount if eligible", example = "0")
    private BigDecimal gratuity = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Other special allowances", example = "1500")
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Recovery for unserved notice period days", example = "0")
    private BigDecimal noticePeriodRecovery = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Recovery for asset damage or unreturned items", example = "0")
    private BigDecimal assetDamage = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Income tax / TDS deduction", example = "4500")
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Recovery for outstanding company loans or advances", example = "0")
    private BigDecimal loanRecovery = BigDecimal.ZERO;

    @PositiveOrZero
    @Schema(description = "Other miscellaneous deductions", example = "0")
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    public FnfCalculationRequest() {}

    public Integer getSalaryDaysWorked() { return salaryDaysWorked != null ? salaryDaysWorked : 0; }
    public void setSalaryDaysWorked(Integer salaryDaysWorked) { this.salaryDaysWorked = salaryDaysWorked; }

    public BigDecimal getUnpaidSalary() { return unpaidSalary != null ? unpaidSalary : BigDecimal.ZERO; }
    public void setUnpaidSalary(BigDecimal unpaidSalary) { this.unpaidSalary = unpaidSalary; }

    public BigDecimal getLeaveEncashment() { return leaveEncashment != null ? leaveEncashment : BigDecimal.ZERO; }
    public void setLeaveEncashment(BigDecimal leaveEncashment) { this.leaveEncashment = leaveEncashment; }

    public BigDecimal getBonus() { return bonus != null ? bonus : BigDecimal.ZERO; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }

    public BigDecimal getIncentives() { return incentives != null ? incentives : BigDecimal.ZERO; }
    public void setIncentives(BigDecimal incentives) { this.incentives = incentives; }

    public BigDecimal getOvertime() { return overtime != null ? overtime : BigDecimal.ZERO; }
    public void setOvertime(BigDecimal overtime) { this.overtime = overtime; }

    public BigDecimal getReimbursements() { return reimbursements != null ? reimbursements : BigDecimal.ZERO; }
    public void setReimbursements(BigDecimal reimbursements) { this.reimbursements = reimbursements; }

    public BigDecimal getGratuity() { return gratuity != null ? gratuity : BigDecimal.ZERO; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getOtherAllowances() { return otherAllowances != null ? otherAllowances : BigDecimal.ZERO; }
    public void setOtherAllowances(BigDecimal otherAllowances) { this.otherAllowances = otherAllowances; }

    public BigDecimal getNoticePeriodRecovery() { return noticePeriodRecovery != null ? noticePeriodRecovery : BigDecimal.ZERO; }
    public void setNoticePeriodRecovery(BigDecimal noticePeriodRecovery) { this.noticePeriodRecovery = noticePeriodRecovery; }

    public BigDecimal getAssetDamage() { return assetDamage != null ? assetDamage : BigDecimal.ZERO; }
    public void setAssetDamage(BigDecimal assetDamage) { this.assetDamage = assetDamage; }

    public BigDecimal getLoanRecovery() { return loanRecovery != null ? loanRecovery : BigDecimal.ZERO; }
    public void setLoanRecovery(BigDecimal loanRecovery) { this.loanRecovery = loanRecovery; }

    public BigDecimal getTaxDeduction() { return taxDeduction != null ? taxDeduction : BigDecimal.ZERO; }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction = taxDeduction; }

    public BigDecimal getOtherDeductions() { return otherDeductions != null ? otherDeductions : BigDecimal.ZERO; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
}
