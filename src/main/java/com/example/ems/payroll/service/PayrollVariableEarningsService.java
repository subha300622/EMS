package com.example.ems.payroll.service;

import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.integration.BonusPayrollAdapter;
import com.example.ems.payroll.integration.IncentivePayrollAdapter;
import com.example.ems.payroll.integration.OvertimePayrollAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class PayrollVariableEarningsService {

    private static final Logger log = LoggerFactory.getLogger(PayrollVariableEarningsService.class);

    private final OvertimePayrollAdapter overtimePayrollAdapter;
    private final IncentivePayrollAdapter incentivePayrollAdapter;
    private final BonusPayrollAdapter bonusPayrollAdapter;

    @Autowired
    public PayrollVariableEarningsService(@Autowired(required = false) OvertimePayrollAdapter overtimePayrollAdapter,
                                          @Autowired(required = false) IncentivePayrollAdapter incentivePayrollAdapter,
                                          @Autowired(required = false) BonusPayrollAdapter bonusPayrollAdapter) {
        this.overtimePayrollAdapter = overtimePayrollAdapter;
        this.incentivePayrollAdapter = incentivePayrollAdapter;
        this.bonusPayrollAdapter = bonusPayrollAdapter;
    }

    /**
     * Aggregates approved variable earnings (Overtime, Incentive, Bonus) for an employee within a payroll period.
     * Respects organization feature flags, returning 0.00 for disabled modules without breaking payroll.
     */
    @Transactional(readOnly = true)
    public VariableEarningsSummaryDto calculateVariableEarnings(Long employeeId, Long organizationId,
                                                               LocalDate periodStart, LocalDate periodEnd,
                                                               int workingDays, SalaryCalculationResponse salaryCalc) {

        // 1. Approved Overtime
        OvertimePeriodSummaryDto otSummary = overtimePayrollAdapter != null
                ? overtimePayrollAdapter.getOvertimeSummary(employeeId, organizationId, periodStart, periodEnd, workingDays, salaryCalc)
                : new OvertimePeriodSummaryDto(0.0, BigDecimal.ZERO, 1.5, BigDecimal.ZERO);
        BigDecimal otAmount = otSummary != null && otSummary.getAmount() != null ? otSummary.getAmount() : BigDecimal.ZERO;

        // 2. Approved Incentive
        IncentivePeriodSummaryDto incentiveSummary = incentivePayrollAdapter != null
                ? incentivePayrollAdapter.getIncentiveSummary(employeeId, organizationId, periodStart, periodEnd)
                : new IncentivePeriodSummaryDto(BigDecimal.ZERO, null, null);
        BigDecimal incentiveAmount = incentiveSummary != null && incentiveSummary.getAmount() != null ? incentiveSummary.getAmount() : BigDecimal.ZERO;

        // 3. Approved Bonus
        BonusPeriodSummaryDto bonusSummary = bonusPayrollAdapter != null
                ? bonusPayrollAdapter.getBonusSummary(employeeId, organizationId, periodStart, periodEnd)
                : new BonusPeriodSummaryDto(BigDecimal.ZERO, null, null);
        BigDecimal bonusAmount = bonusSummary != null && bonusSummary.getAmount() != null ? bonusSummary.getAmount() : BigDecimal.ZERO;

        VariableEarningsSummaryDto result = new VariableEarningsSummaryDto(
                otAmount, incentiveAmount, bonusAmount, otSummary, incentiveSummary, bonusSummary
        );

        log.debug("Employee ID={} Org ID={} Period [{} to {}] Variable Earnings: OT={}, Incentive={}, Bonus={}, Total={}",
                employeeId, organizationId, periodStart, periodEnd, otAmount, incentiveAmount, bonusAmount, result.getTotalVariableEarnings());

        return result;
    }

    /**
     * Atomically marks approved variable compensation records (OT, Incentive, Bonus) as POSTED to the payroll run.
     * Prevents double-counting and duplicate posting on payroll retry.
     */
    public void markVariableEarningsPosted(Long employeeId, Long organizationId, LocalDate periodStart, Long payrollRunId,
                                          VariableEarningsSummaryDto summary) {
        if (summary == null) return;

        if (summary.getOvertimeAmount().compareTo(BigDecimal.ZERO) > 0 && overtimePayrollAdapter != null) {
            overtimePayrollAdapter.markOvertimeProcessed(employeeId, organizationId, periodStart, payrollRunId);
        }

        if (summary.getIncentiveAmount().compareTo(BigDecimal.ZERO) > 0 && incentivePayrollAdapter != null) {
            incentivePayrollAdapter.markIncentivesProcessed(employeeId, organizationId, periodStart, payrollRunId);
        }

        if (summary.getBonusAmount().compareTo(BigDecimal.ZERO) > 0 && bonusPayrollAdapter != null) {
            bonusPayrollAdapter.markBonusesProcessed(employeeId, organizationId, periodStart, payrollRunId);
        }
    }
}
