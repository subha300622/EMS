package com.example.ems.payroll.service;

import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.integration.BonusPayrollAdapter;
import com.example.ems.payroll.integration.IncentivePayrollAdapter;
import com.example.ems.payroll.integration.OvertimePayrollAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollVariableEarningsServiceTest {

    @Mock
    private OvertimePayrollAdapter overtimePayrollAdapter;

    @Mock
    private IncentivePayrollAdapter incentivePayrollAdapter;

    @Mock
    private BonusPayrollAdapter bonusPayrollAdapter;

    private PayrollVariableEarningsService variableEarningsService;

    private final Long employeeId = 101L;
    private final Long organizationId = 1L;
    private final LocalDate periodStart = LocalDate.of(2026, 9, 1);
    private final LocalDate periodEnd = LocalDate.of(2026, 9, 30);
    private final int workingDays = 22;

    @BeforeEach
    void setUp() {
        variableEarningsService = new PayrollVariableEarningsService(
                overtimePayrollAdapter,
                incentivePayrollAdapter,
                bonusPayrollAdapter
        );
    }

    @Test
    @DisplayName("Should aggregate OT + Incentive + Bonus when all modules return approved earnings")
    void testCalculateVariableEarnings_AllEnabled_CombinesOTIncentiveBonus() {
        SalaryCalculationResponse salaryCalc = new SalaryCalculationResponse();
        salaryCalc.setEmployeeId(employeeId);
        salaryCalc.setGrossPay(BigDecimal.valueOf(40000));
        salaryCalc.setTotalDeductions(BigDecimal.valueOf(4000));
        salaryCalc.setNetPay(BigDecimal.valueOf(36000));
        salaryCalc.setCurrency("INR");

        OvertimePeriodSummaryDto otSummary = new OvertimePeriodSummaryDto(
                10.0, BigDecimal.valueOf(200.00), 1.5, BigDecimal.valueOf(3000.00)
        );
        IncentivePeriodSummaryDto incSummary = new IncentivePeriodSummaryDto(
                BigDecimal.valueOf(5000.00), null, null
        );
        BonusPeriodSummaryDto bonusSummary = new BonusPeriodSummaryDto(
                BigDecimal.valueOf(10000.00), null, null
        );

        when(overtimePayrollAdapter.getOvertimeSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd), eq(workingDays), any()))
                .thenReturn(otSummary);
        when(incentivePayrollAdapter.getIncentiveSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd)))
                .thenReturn(incSummary);
        when(bonusPayrollAdapter.getBonusSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd)))
                .thenReturn(bonusSummary);

        VariableEarningsSummaryDto result = variableEarningsService.calculateVariableEarnings(
                employeeId, organizationId, periodStart, periodEnd, workingDays, salaryCalc
        );

        assertThat(result).isNotNull();
        assertThat(result.getOvertimeAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(result.getIncentiveAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getBonusAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
        assertThat(result.getTotalVariableEarnings()).isEqualByComparingTo(BigDecimal.valueOf(18000.00));
        assertThat(result.getOvertimeSummary()).isEqualTo(otSummary);
        assertThat(result.getIncentiveSummary()).isEqualTo(incSummary);
        assertThat(result.getBonusSummary()).isEqualTo(bonusSummary);
    }

    @Test
    @DisplayName("Should handle partial feature enablement (e.g. OT ON, Incentive/Bonus OFF) gracefully")
    void testCalculateVariableEarnings_PartialEnablement_ReturnsExpectedTotal() {
        SalaryCalculationResponse salaryCalc = new SalaryCalculationResponse();
        salaryCalc.setEmployeeId(employeeId);
        salaryCalc.setGrossPay(BigDecimal.valueOf(40000));
        salaryCalc.setTotalDeductions(BigDecimal.ZERO);
        salaryCalc.setNetPay(BigDecimal.valueOf(40000));
        salaryCalc.setCurrency("INR");

        OvertimePeriodSummaryDto otSummary = new OvertimePeriodSummaryDto(
                8.0, BigDecimal.valueOf(200.00), 1.5, BigDecimal.valueOf(2400.00)
        );

        when(overtimePayrollAdapter.getOvertimeSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd), eq(workingDays), any()))
                .thenReturn(otSummary);
        when(incentivePayrollAdapter.getIncentiveSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd)))
                .thenReturn(new IncentivePeriodSummaryDto(BigDecimal.ZERO, null, null));
        when(bonusPayrollAdapter.getBonusSummary(eq(employeeId), eq(organizationId), eq(periodStart), eq(periodEnd)))
                .thenReturn(new BonusPeriodSummaryDto(BigDecimal.ZERO, null, null));

        VariableEarningsSummaryDto result = variableEarningsService.calculateVariableEarnings(
                employeeId, organizationId, periodStart, periodEnd, workingDays, salaryCalc
        );

        assertThat(result.getOvertimeAmount()).isEqualByComparingTo(BigDecimal.valueOf(2400.00));
        assertThat(result.getIncentiveAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBonusAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalVariableEarnings()).isEqualByComparingTo(BigDecimal.valueOf(2400.00));
    }

    @Test
    @DisplayName("Should return 0.00 if adapters are null without throwing NullPointerException")
    void testCalculateVariableEarnings_NullAdapters_ReturnsZeros() {
        PayrollVariableEarningsService serviceWithNulls = new PayrollVariableEarningsService(null, null, null);

        VariableEarningsSummaryDto result = serviceWithNulls.calculateVariableEarnings(
                employeeId, organizationId, periodStart, periodEnd, workingDays, null
        );

        assertThat(result).isNotNull();
        assertThat(result.getOvertimeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getIncentiveAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBonusAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalVariableEarnings()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should mark OT, Incentive, and Bonus as posted when amounts > 0")
    void testMarkVariableEarningsPosted_DelegatesToAdapters() {
        Long payrollRunId = 1001L;
        VariableEarningsSummaryDto summary = new VariableEarningsSummaryDto(
                BigDecimal.valueOf(3000.00),
                BigDecimal.valueOf(5000.00),
                BigDecimal.valueOf(10000.00),
                null, null, null
        );

        variableEarningsService.markVariableEarningsPosted(
                employeeId, organizationId, periodStart, payrollRunId, summary
        );

        verify(overtimePayrollAdapter, times(1)).markOvertimeProcessed(employeeId, organizationId, periodStart, payrollRunId);
        verify(incentivePayrollAdapter, times(1)).markIncentivesProcessed(employeeId, organizationId, periodStart, payrollRunId);
        verify(bonusPayrollAdapter, times(1)).markBonusesProcessed(employeeId, organizationId, periodStart, payrollRunId);
    }

    @Test
    @DisplayName("Should not call adapters when amounts are zero")
    void testMarkVariableEarningsPosted_ZeroAmounts_DoesNotCallAdapters() {
        Long payrollRunId = 1001L;
        VariableEarningsSummaryDto summary = new VariableEarningsSummaryDto(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null, null, null
        );

        variableEarningsService.markVariableEarningsPosted(
                employeeId, organizationId, periodStart, payrollRunId, summary
        );

        verify(overtimePayrollAdapter, never()).markOvertimeProcessed(anyLong(), anyLong(), any(), anyLong());
        verify(incentivePayrollAdapter, never()).markIncentivesProcessed(anyLong(), anyLong(), any(), anyLong());
        verify(bonusPayrollAdapter, never()).markBonusesProcessed(anyLong(), anyLong(), any(), anyLong());
    }
}
