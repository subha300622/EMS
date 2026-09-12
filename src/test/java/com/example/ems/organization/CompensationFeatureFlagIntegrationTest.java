package com.example.ems.organization;

import com.example.ems.bonus.dto.BonusCalculateRequest;
import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.entity.BonusCalculationMethod;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.repository.BonusPolicyRepository;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.bonus.service.BonusCalculationService;
import com.example.ems.bonus.service.BonusEligibilityService;
import com.example.ems.bonus.service.BonusPolicyService;
import com.example.ems.bonus.service.BonusWorkflowService;
import com.example.ems.common.exception.ModuleDisabledException;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.IncentiveCalculateRequest;
import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentiveType;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.incentive.service.IncentiveCalculationService;
import com.example.ems.incentive.service.IncentiveEligibilityService;
import com.example.ems.incentive.service.IncentivePolicyService;
import com.example.ems.incentive.service.IncentiveWorkflowService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationCompensationConfigRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.repository.OvertimePolicyRepository;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.overtime.service.OvertimeCalculationService;
import com.example.ems.overtime.service.OvertimePolicyService;
import com.example.ems.overtime.service.OvertimeWorkflowService;
import com.example.ems.payroll.dto.BonusPeriodSummaryDto;
import com.example.ems.payroll.dto.IncentivePeriodSummaryDto;
import com.example.ems.payroll.dto.OvertimePeriodSummaryDto;
import com.example.ems.payroll.integration.BonusPayrollAdapter;
import com.example.ems.payroll.integration.IncentivePayrollAdapter;
import com.example.ems.payroll.integration.OvertimePayrollAdapter;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompensationFeatureFlagIntegrationTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private OrganizationCompensationConfigRepository configRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BonusPolicyRepository bonusPolicyRepository;

    @Mock
    private BonusRecordRepository bonusRecordRepository;

    @Mock
    private IncentivePolicyRepository incentivePolicyRepository;

    @Mock
    private IncentiveRecordRepository incentiveRecordRepository;

    @Mock
    private OvertimePolicyRepository overtimePolicyRepository;

    @Mock
    private OvertimeRecordRepository overtimeRecordRepository;

    @Mock
    private BonusEligibilityService bonusEligibilityService;

    @Mock
    private IncentiveEligibilityService incentiveEligibilityService;

    private OrganizationCompensationConfigService configService;
    private BonusPolicyService bonusPolicyService;
    private BonusCalculationService bonusCalculationService;
    private BonusWorkflowService bonusWorkflowService;

    private IncentivePolicyService incentivePolicyService;
    private IncentiveCalculationService incentiveCalculationService;
    private IncentiveWorkflowService incentiveWorkflowService;

    private OvertimePolicyService overtimePolicyService;
    private OvertimeCalculationService overtimeCalculationService;
    private OvertimeWorkflowService overtimeWorkflowService;

    private BonusPayrollAdapter bonusPayrollAdapter;
    private IncentivePayrollAdapter incentivePayrollAdapter;
    private OvertimePayrollAdapter overtimePayrollAdapter;

    private Organization organization;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Test Org");

        configService = new OrganizationCompensationConfigService(configRepository, organizationRepository);

        bonusPolicyService = new BonusPolicyService(bonusPolicyRepository, organizationRepository, configService);
        bonusCalculationService = new BonusCalculationService(bonusPolicyService, bonusEligibilityService, bonusPolicyRepository, bonusRecordRepository, employeeRepository, null, null, configService);
        bonusWorkflowService = new BonusWorkflowService(bonusRecordRepository, null, configService);

        incentivePolicyService = new IncentivePolicyService(incentivePolicyRepository, organizationRepository, configService);
        incentiveCalculationService = new IncentiveCalculationService(incentivePolicyService, incentiveEligibilityService, incentivePolicyRepository, incentiveRecordRepository, employeeRepository, configService);
        incentiveWorkflowService = new IncentiveWorkflowService(incentiveRecordRepository, null, configService);

        overtimePolicyService = new OvertimePolicyService(overtimePolicyRepository, organizationRepository, configService);
        overtimeCalculationService = new OvertimeCalculationService(overtimePolicyService, overtimeRecordRepository, null, null, configService);
        overtimeWorkflowService = new OvertimeWorkflowService(overtimeRecordRepository, null, configService);

        bonusPayrollAdapter = new BonusPayrollAdapter();
        // inject configService via reflection or field
        org.springframework.test.util.ReflectionTestUtils.setField(bonusPayrollAdapter, "compensationConfigService", configService);
        org.springframework.test.util.ReflectionTestUtils.setField(bonusPayrollAdapter, "bonusRecordRepository", bonusRecordRepository);

        incentivePayrollAdapter = new IncentivePayrollAdapter();
        org.springframework.test.util.ReflectionTestUtils.setField(incentivePayrollAdapter, "compensationConfigService", configService);
        org.springframework.test.util.ReflectionTestUtils.setField(incentivePayrollAdapter, "incentiveRecordRepository", incentiveRecordRepository);

        overtimePayrollAdapter = new OvertimePayrollAdapter(null);
        org.springframework.test.util.ReflectionTestUtils.setField(overtimePayrollAdapter, "compensationConfigService", configService);
        org.springframework.test.util.ReflectionTestUtils.setField(overtimePayrollAdapter, "overtimeRecordRepository", overtimeRecordRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("When Bonus is DISABLED: Policy, Preview, Calculate throw BONUS_MODULE_DISABLED, Payroll returns 0")
    void testBonusModuleDisabled() {
        com.example.ems.organization.entity.OrganizationCompensationConfig cfg =
                new com.example.ems.organization.entity.OrganizationCompensationConfig(organization);
        cfg.setBonusEnabled(false);
        cfg.setIncentiveEnabled(true);
        cfg.setOvertimeEnabled(true);

        when(configRepository.findByOrganizationId(ORG_ID)).thenReturn(Optional.of(cfg));

        // 1. Bonus Policy create should fail
        BonusPolicyRequest policyReq = new BonusPolicyRequest();
        policyReq.setName("Yearly Bonus");
        policyReq.setBonusType(BonusType.PERFORMANCE);
        policyReq.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        policyReq.setFixedAmount(BigDecimal.valueOf(10000));
        policyReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> bonusPolicyService.createPolicy(policyReq))
                .isInstanceOf(ModuleDisabledException.class)
                .hasMessage("Bonus module is not enabled for this organization")
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("BONUS_MODULE_DISABLED"));

        // 2. Bonus Preview & Calculate should fail
        BonusCalculateRequest calcReq = new BonusCalculateRequest();
        calcReq.setEmployeeId(1L);
        calcReq.setPeriodStart(LocalDate.of(2026, 1, 1));
        calcReq.setPeriodEnd(LocalDate.of(2026, 12, 31));

        assertThatThrownBy(() -> bonusCalculationService.previewBonus(calcReq))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("BONUS_MODULE_DISABLED"));

        assertThatThrownBy(() -> bonusCalculationService.calculateAndPersist(calcReq))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("BONUS_MODULE_DISABLED"));

        // 3. Bonus Submit should fail
        assertThatThrownBy(() -> bonusWorkflowService.submitBonus(1L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("BONUS_MODULE_DISABLED"));

        // 4. Payroll Adapter should NOT fail, but return 0
        BonusPeriodSummaryDto bonusSummary = bonusPayrollAdapter.getBonusSummary(
                1L, ORG_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(bonusSummary.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bonusSummary.getBonusIds()).isEmpty();
    }

    @Test
    @DisplayName("When Incentive is DISABLED: Policy, Preview, Calculate throw INCENTIVE_MODULE_DISABLED, Payroll returns 0")
    void testIncentiveModuleDisabled() {
        com.example.ems.organization.entity.OrganizationCompensationConfig cfg =
                new com.example.ems.organization.entity.OrganizationCompensationConfig(organization);
        cfg.setBonusEnabled(true);
        cfg.setIncentiveEnabled(false);
        cfg.setOvertimeEnabled(true);

        when(configRepository.findByOrganizationId(ORG_ID)).thenReturn(Optional.of(cfg));

        // 1. Incentive Policy create should fail
        IncentivePolicyRequest policyReq = new IncentivePolicyRequest();
        policyReq.setName("Sales Incentive");
        policyReq.setIncentiveType(IncentiveType.SALES);
        policyReq.setCalculationMethod(IncentiveCalculationMethod.PERCENTAGE_OF_ACHIEVEMENT);
        policyReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> incentivePolicyService.createPolicy(policyReq))
                .isInstanceOf(ModuleDisabledException.class)
                .hasMessage("Incentive module is not enabled for this organization")
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("INCENTIVE_MODULE_DISABLED"));

        // 2. Incentive Preview & Calculate should fail
        IncentiveCalculateRequest calcReq = new IncentiveCalculateRequest();
        calcReq.setEmployeeId(1L);
        calcReq.setPeriodStart(LocalDate.of(2026, 1, 1));
        calcReq.setPeriodEnd(LocalDate.of(2026, 1, 31));

        assertThatThrownBy(() -> incentiveCalculationService.previewIncentive(calcReq))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("INCENTIVE_MODULE_DISABLED"));

        assertThatThrownBy(() -> incentiveCalculationService.calculateAndPersist(calcReq))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("INCENTIVE_MODULE_DISABLED"));

        // 3. Incentive Submit should fail
        assertThatThrownBy(() -> incentiveWorkflowService.submitIncentive(1L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("INCENTIVE_MODULE_DISABLED"));

        // 4. Payroll Adapter should NOT fail, but return 0
        IncentivePeriodSummaryDto incentiveSummary = incentivePayrollAdapter.getIncentiveSummary(
                1L, ORG_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(incentiveSummary.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(incentiveSummary.getIncentiveIds()).isEmpty();
    }

    @Test
    @DisplayName("When Overtime is DISABLED: Policy, Preview, Calculate throw OVERTIME_MODULE_DISABLED, Payroll returns 0")
    void testOvertimeModuleDisabled() {
        com.example.ems.organization.entity.OrganizationCompensationConfig cfg =
                new com.example.ems.organization.entity.OrganizationCompensationConfig(organization);
        cfg.setBonusEnabled(true);
        cfg.setIncentiveEnabled(true);
        cfg.setOvertimeEnabled(false);

        when(configRepository.findByOrganizationId(ORG_ID)).thenReturn(Optional.of(cfg));

        // 1. Overtime Policy create should fail
        OvertimePolicyRequest policyReq = new OvertimePolicyRequest();
        policyReq.setName("Standard OT");
        policyReq.setNormalDayMultiplier(BigDecimal.valueOf(1.5));
        policyReq.setAmountBasis(OvertimeAmountBasis.GROSS_SALARY);
        policyReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> overtimePolicyService.createPolicy(policyReq))
                .isInstanceOf(ModuleDisabledException.class)
                .hasMessage("Overtime module is not enabled for this organization")
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("OVERTIME_MODULE_DISABLED"));

        // 2. Overtime Preview & Calculate should fail
        assertThatThrownBy(() -> overtimeCalculationService.previewOvertime(1L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("OVERTIME_MODULE_DISABLED"));

        assertThatThrownBy(() -> overtimeCalculationService.calculateOvertime(1L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("OVERTIME_MODULE_DISABLED"));

        // 3. Overtime Submit should fail
        assertThatThrownBy(() -> overtimeWorkflowService.submitOvertime(1L))
                .isInstanceOf(ModuleDisabledException.class)
                .satisfies(e -> assertThat(((ModuleDisabledException) e).getModuleCode()).isEqualTo("OVERTIME_MODULE_DISABLED"));

        // 4. Payroll Adapter should NOT fail, but return 0
        OvertimePeriodSummaryDto otSummary = overtimePayrollAdapter.getOvertimeSummary(
                1L, ORG_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 22, null);

        assertThat(otSummary.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(otSummary.getApprovedHours()).isEqualTo(0.0);
    }
}
