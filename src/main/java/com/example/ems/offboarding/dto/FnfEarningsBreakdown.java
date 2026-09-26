package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Detailed breakdown of F&F Earnings")
public class FnfEarningsBreakdown {

    @Schema(description = "Base prorated monthly salary", example = "55000.00")
    private BigDecimal salary = BigDecimal.ZERO;

    @Schema(description = "Pending/unpaid salary arrears from prior pay periods", example = "15000.00")
    private BigDecimal unpaidSalary = BigDecimal.ZERO;

    @Schema(description = "Cash payout for accrued earned leaves", example = "12500.00")
    private BigDecimal leaveEncashment = BigDecimal.ZERO;

    @Schema(description = "Prorated or performance bonus payout", example = "10000.00")
    private BigDecimal bonus = BigDecimal.ZERO;

    @Schema(description = "Sales or achievement incentive payout", example = "5000.00")
    private BigDecimal incentives = BigDecimal.ZERO;

    @Schema(description = "Overtime pay", example = "2000.00")
    private BigDecimal overtime = BigDecimal.ZERO;

    @Schema(description = "Approved pending expense reimbursements", example = "3500.00")
    private BigDecimal reimbursements = BigDecimal.ZERO;

    @Schema(description = "Statutory gratuity payout if tenure threshold is met", example = "0.00")
    private BigDecimal gratuity = BigDecimal.ZERO;

    @Schema(description = "Other special allowances or retiral benefits", example = "1500.00")
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @Schema(description = "Total aggregate gross earnings", example = "104000.00")
    private BigDecimal total = BigDecimal.ZERO;

    public FnfEarningsBreakdown() {}

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public BigDecimal getUnpaidSalary() { return unpaidSalary; }
    public void setUnpaidSalary(BigDecimal unpaidSalary) { this.unpaidSalary = unpaidSalary; }

    public BigDecimal getLeaveEncashment() { return leaveEncashment; }
    public void setLeaveEncashment(BigDecimal leaveEncashment) { this.leaveEncashment = leaveEncashment; }

    public BigDecimal getBonus() { return bonus; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }

    public BigDecimal getIncentives() { return incentives; }
    public void setIncentives(BigDecimal incentives) { this.incentives = incentives; }

    public BigDecimal getOvertime() { return overtime; }
    public void setOvertime(BigDecimal overtime) { this.overtime = overtime; }

    public BigDecimal getReimbursements() { return reimbursements; }
    public void setReimbursements(BigDecimal reimbursements) { this.reimbursements = reimbursements; }

    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getOtherAllowances() { return otherAllowances; }
    public void setOtherAllowances(BigDecimal otherAllowances) { this.otherAllowances = otherAllowances; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}

