package com.example.ems.incentive.service;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.IncentiveCalculateRequest;
import com.example.ems.incentive.dto.IncentivePreviewResponse;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.dto.IncentiveSlabTierDto;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncentiveCalculationServiceTest {

    @Mock
    private IncentivePolicyService policyService;

    @Mock
    private IncentiveEligibilityService eligibilityService;

    @Mock
    private IncentivePolicyRepository policyRepository;

    @Mock
    private IncentiveRecordRepository recordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @InjectMocks
    private IncentiveCalculationService calculationService;

    private Organization organization;
    private Employee employee;
    private IncentivePolicy policy;
    private final Long orgId = 1L;
    private final Long empId = 10L;
    private final Long polId = 100L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        organization = new Organization();
        organization.setId(orgId);

        employee = new Employee();
        employee.setId(empId);
        employee.setOrganization(organization);
        employee.setFullName("John Doe");
        employee.setStatus("ACTIVE");
        employee.setEmploymentType("FULL_TIME");
        employee.setAnnualSalary(BigDecimal.valueOf(600000.00)); // annual -> basic = 25000 / month

        policy = new IncentivePolicy();
        policy.setId(polId);
        policy.setOrganization(organization);
        policy.setName("Standard Sales Incentive");
        policy.setIncentiveType(IncentiveType.SALES);
        policy.setStatus(IncentivePolicyStatus.ACTIVE);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setEffectiveTo(LocalDate.of(2026, 12, 31));
        policy.setPolicyVersion(1L);
        policy.setApprovalRequired(true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testFixedAmountCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(5000.00));

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> {
            IncentiveRecord r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(5000.00).setScale(2), resp.getCalculatedAmount());
        assertEquals(IncentiveStatus.CALCULATED, resp.getStatus());
        assertEquals(IncentivePayrollStatus.PENDING, resp.getPayrollStatus());
    }

    @Test
    void testPercentageOfBasicCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.PERCENTAGE_OF_BASIC);
        policy.setPercentage(BigDecimal.valueOf(10.0)); // 10% of basic (25,000) = 2,500

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(2500.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testPercentageOfGrossCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.PERCENTAGE_OF_GROSS);
        policy.setPercentage(BigDecimal.valueOf(5.0)); // 5% of gross (50,000) = 2,500

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(2500.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testPercentageOfAchievementCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.PERCENTAGE_OF_ACHIEVEMENT);
        policy.setPercentage(BigDecimal.valueOf(5.0)); // 5% of 100,000 achieved = 5,000
        policy.setTargetValue(BigDecimal.valueOf(80000.00));

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setAchievedValue(BigDecimal.valueOf(100000.00));
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(5000.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testTargetSlabCalculation() throws Exception {
        policy.setCalculationMethod(IncentiveCalculationMethod.TARGET_SLAB);
        policy.setTargetValue(BigDecimal.valueOf(1000000.00));

        List<IncentiveSlabTierDto> slabs = List.of(
                new IncentiveSlabTierDto(BigDecimal.valueOf(80.0), BigDecimal.valueOf(89.99), BigDecimal.valueOf(2000.00), null),
                new IncentiveSlabTierDto(BigDecimal.valueOf(90.0), BigDecimal.valueOf(99.99), BigDecimal.valueOf(4000.00), null),
                new IncentiveSlabTierDto(BigDecimal.valueOf(100.0), BigDecimal.valueOf(109.99), BigDecimal.valueOf(6000.00), null),
                new IncentiveSlabTierDto(BigDecimal.valueOf(110.0), null, BigDecimal.valueOf(8000.00), null)
        );
        policy.setTargetSlabsJson(objectMapper.writeValueAsString(slabs));

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setAchievedValue(BigDecimal.valueOf(1200000.00)); // 120% achievement -> matches >= 110% tier (8,000)
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(8000.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testPerformanceRatingCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.PERFORMANCE_RATING);
        policy.setFixedAmount(BigDecimal.valueOf(5000.00)); // 5,000 * (4.5 / 5.0) = 4,500

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPerformanceRating(BigDecimal.valueOf(4.5));
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(4500.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testSafeFormulaCalculation() {
        policy.setCalculationMethod(IncentiveCalculationMethod.FORMULA);
        policy.setFormulaExpression("basic * 0.10 + 500"); // 25,000 * 0.10 + 500 = 3,000

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(3000.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testCalculationCapsEnforced() {
        policy.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(10000.00));
        policy.setMaximumAmount(BigDecimal.valueOf(6000.00)); // Cap at 6,000

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = calculationService.calculateIncentive(req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(6000.00).setScale(2), resp.getCalculatedAmount());
    }

    @Test
    void testDuplicateCalculationRejected() {
        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
                orgId, empId, polId, req.getPeriodStart(), req.getPeriodEnd())).thenReturn(true);

        assertThrows(ConflictException.class, () -> calculationService.calculateIncentive(req));
    }

    @Test
    void testPreviewDoesNotPersist() {
        policy.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(5000.00));

        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(empId);
        req.setPolicyId(polId);
        req.setPeriodStart(LocalDate.of(2026, 9, 1));
        req.setPeriodEnd(LocalDate.of(2026, 9, 30));

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(policyRepository.findByIdAndOrganizationId(polId, orgId)).thenReturn(Optional.of(policy));
        when(eligibilityService.checkEligibility(any(), any(), any(), any(), any(), any()))
                .thenReturn(new IncentiveEligibilityService.EligibilityResult(true, "Eligible"));

        IncentivePreviewResponse preview = calculationService.previewIncentive(req);

        assertNotNull(preview);
        assertEquals(BigDecimal.valueOf(5000.00).setScale(2), preview.getCalculatedAmount());
        assertTrue(preview.getEligible());
        verify(recordRepository, never()).save(any());
    }
}
