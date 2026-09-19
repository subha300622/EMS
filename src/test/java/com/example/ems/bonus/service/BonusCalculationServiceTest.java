package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusCalculateRequest;
import com.example.ems.bonus.dto.BonusPreviewResponse;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.*;
import com.example.ems.bonus.repository.BonusPolicyRepository;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.payroll.dto.SalaryCalculatedComponentResponse;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.service.SalaryCalculationService;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusCalculationServiceTest {

    @Mock
    private BonusPolicyService policyService;

    @Mock
    private BonusEligibilityService eligibilityService;

    @Mock
    private BonusPolicyRepository policyRepository;

    @Mock
    private BonusRecordRepository recordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryCalculationService salaryCalculationService;

    @InjectMocks
    private BonusCalculationService calculationService;

    private Organization organization;
    private Employee employee;
    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        organization = new Organization();
        organization.setId(orgId);

        employee = new Employee();
        employee.setId(101L);
        employee.setOrganization(organization);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP-101");
        employee.setStatus("ACTIVE");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testPreview_FixedAmount() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(5L);
        policy.setName("Annual Bonus");
        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setBonusType(BonusType.ANNUAL);
        policy.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(15000.00));
        policy.setEffectiveFrom(LocalDate.of(2026, 4, 1));
        policy.setEffectiveTo(LocalDate.of(2027, 3, 31));

        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        when(employeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any()))
                .thenReturn(BonusEligibilityService.EligibilityResult.eligible());

        BonusPreviewResponse resp = calculationService.previewBonus(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(15000.00).setScale(2), resp.getCalculatedAmount());
        assertTrue(resp.isEligible());
        assertEquals(5L, resp.getPolicyId());
    }

    @Test
    void testCalculateAndPersist_PercentageOfBasic() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(5L);
        policy.setName("Basic % Bonus");
        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setBonusType(BonusType.PERFORMANCE);
        policy.setCalculationMethod(BonusCalculationMethod.PERCENTAGE_OF_BASIC);
        policy.setPercentage(BigDecimal.valueOf(10.0)); // 10%
        policy.setEffectiveFrom(LocalDate.of(2026, 4, 1));
        policy.setEffectiveTo(LocalDate.of(2027, 3, 31));

        SalaryCalculatedComponentResponse basicComp = new SalaryCalculatedComponentResponse();
        basicComp.setComponentCode("BASIC");
        basicComp.setAmount(BigDecimal.valueOf(40000.00));

        SalaryCalculationResponse salaryResp = new SalaryCalculationResponse();
        salaryResp.setGrossPay(BigDecimal.valueOf(60000.00));
        salaryResp.setComponents(java.util.List.of(basicComp));

        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        when(employeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(policy));
        when(salaryCalculationService.calculateSalaryForDate(eq(101L), any())).thenReturn(salaryResp);
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any()))
                .thenReturn(BonusEligibilityService.EligibilityResult.eligible());
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> {
            BonusRecord r = i.getArgument(0);
            r.setId(501L);
            return r;
        });

        BonusRecordResponse resp = calculationService.calculateAndPersist(req);

        assertNotNull(resp);
        assertEquals(501L, resp.getId());
        // 40,000 * 10% = 4,000.00
        assertEquals(BigDecimal.valueOf(4000.00).setScale(2), resp.getCalculatedAmount());
        assertEquals(BonusStatus.CALCULATED, resp.getStatus());
        assertEquals(BonusPayrollStatus.PENDING, resp.getPayrollStatus());
    }

    @Test
    void testCalculateAndPersist_DuplicateRejected() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(5L);

        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        when(employeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(policy));
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
                orgId, 101L, 5L, req.getPeriodStart(), req.getPeriodEnd())).thenReturn(true);

        assertThrows(ConflictException.class, () -> calculationService.calculateAndPersist(req));
    }

    @Test
    void testCalculateAndPersist_Discretionary() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(5L);
        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setBonusType(BonusType.DISCRETIONARY);
        policy.setCalculationMethod(BonusCalculationMethod.DISCRETIONARY);
        policy.setEffectiveFrom(LocalDate.of(2026, 4, 1));

        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));
        req.setDiscretionaryAmount(BigDecimal.valueOf(25000.00));

        when(employeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(policy));
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any()))
                .thenReturn(BonusEligibilityService.EligibilityResult.eligible());
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> {
            BonusRecord r = i.getArgument(0);
            r.setId(601L);
            return r;
        });

        BonusRecordResponse resp = calculationService.calculateAndPersist(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(25000.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testCalculateAndPersist_MinMaxClamping() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(5L);
        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(60000.00)); // Above max
        policy.setMinimumAmount(BigDecimal.valueOf(5000.00));
        policy.setMaximumAmount(BigDecimal.valueOf(50000.00));
        policy.setEffectiveFrom(LocalDate.of(2026, 4, 1));

        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        when(employeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(5L, orgId)).thenReturn(Optional.of(policy));
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any()))
                .thenReturn(BonusEligibilityService.EligibilityResult.eligible());
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> i.getArgument(0));

        BonusRecordResponse resp = calculationService.calculateAndPersist(req);

        // Clamped from 60,000 to max 50,000.00
        assertEquals(BigDecimal.valueOf(50000.00).setScale(2), resp.getCalculatedAmount());
    }
}
