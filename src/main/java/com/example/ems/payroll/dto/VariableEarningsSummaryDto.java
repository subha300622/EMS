package com.example.ems.payroll.dto;

import java.math.BigDecimal;

public class VariableEarningsSummaryDto {

    private BigDecimal overtimeAmount = BigDecimal.ZERO;
    private BigDecimal incentiveAmount = BigDecimal.ZERO;
    private BigDecimal bonusAmount = BigDecimal.ZERO;
    private BigDecimal totalVariableEarnings = BigDecimal.ZERO;

    private OvertimePeriodSummaryDto overtimeSummary;
    private IncentivePeriodSummaryDto incentiveSummary;
    private BonusPeriodSummaryDto bonusSummary;

    public VariableEarningsSummaryDto() {}

    public VariableEarningsSummaryDto(BigDecimal overtimeAmount,
                                      BigDecimal incentiveAmount,
                                      BigDecimal bonusAmount,
                                      OvertimePeriodSummaryDto overtimeSummary,
                                      IncentivePeriodSummaryDto incentiveSummary,
                                      BonusPeriodSummaryDto bonusSummary) {
        this.overtimeAmount = overtimeAmount != null ? overtimeAmount : BigDecimal.ZERO;
        this.incentiveAmount = incentiveAmount != null ? incentiveAmount : BigDecimal.ZERO;
        this.bonusAmount = bonusAmount != null ? bonusAmount : BigDecimal.ZERO;
        this.totalVariableEarnings = this.overtimeAmount.add(this.incentiveAmount).add(this.bonusAmount);
        this.overtimeSummary = overtimeSummary;
        this.incentiveSummary = incentiveSummary;
        this.bonusSummary = bonusSummary;
    }

    public BigDecimal getOvertimeAmount() {
        return overtimeAmount;
    }

    public void setOvertimeAmount(BigDecimal overtimeAmount) {
        this.overtimeAmount = overtimeAmount;
    }

    public BigDecimal getIncentiveAmount() {
        return incentiveAmount;
    }

    public void setIncentiveAmount(BigDecimal incentiveAmount) {
        this.incentiveAmount = incentiveAmount;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public BigDecimal getTotalVariableEarnings() {
        return totalVariableEarnings;
    }

    public void setTotalVariableEarnings(BigDecimal totalVariableEarnings) {
        this.totalVariableEarnings = totalVariableEarnings;
    }

    public OvertimePeriodSummaryDto getOvertimeSummary() {
        return overtimeSummary;
    }

    public void setOvertimeSummary(OvertimePeriodSummaryDto overtimeSummary) {
        this.overtimeSummary = overtimeSummary;
    }

    public IncentivePeriodSummaryDto getIncentiveSummary() {
        return incentiveSummary;
    }

    public void setIncentiveSummary(IncentivePeriodSummaryDto incentiveSummary) {
        this.incentiveSummary = incentiveSummary;
    }

    public BonusPeriodSummaryDto getBonusSummary() {
        return bonusSummary;
    }

    public void setBonusSummary(BonusPeriodSummaryDto bonusSummary) {
        this.bonusSummary = bonusSummary;
    }
}
