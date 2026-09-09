package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.eligibility.IncrementEligibilityService;
import com.example.ems.increment.entity.*;
import com.example.ems.increment.repository.IncrementCycleRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment Recommendation Service Unit Tests")
public class IncrementRecommendationServiceTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private IncrementCycleRepository cycleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private IncrementEligibilityService eligibilityService;

    @InjectMocks
    private IncrementRecommendationService recommendationService;

    private Organization organization;
    private IncrementPolicy policy;
    private IncrementCycle cycle;
    private Employee employee;
    private Appraisal appraisal;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setName("FY2026 Policy");
        policy.setOrganization(organization);
        policy.setAppraisalRequired(true);
        policy.setMinimumIncrementPercentage(3.0);
        policy.setMaximumIncrementPercentage(20.0);

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setName("Annual Cycle 2026");
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setStatus(IncrementCycleStatus.OPEN);
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setEffectiveDate(LocalDate.of(2026, 10, 1));

        employee = new Employee();
        employee.setId(125L);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP125");
        employee.setStatus("ACTIVE");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setAnnualSalary(new BigDecimal("50000.00"));

        appraisal = new Appraisal();
        appraisal.setId(501L);
        appraisal.setEmployee(employee);
        appraisal.setOrganization(organization);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal.setFinalRating(4.5);

        try {
            var f = IncrementRecommendationService.class.getDeclaredField("appraisalResolver");
            f.setAccessible(true);
            f.set(recommendationService, new IncrementAppraisalResolver(appraisalRepository));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("✅ 5% Increment: Amount = ₹2,500, New Salary = ₹52,500")
    void testCreateRecommendation_5Percent_Success() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(5.0);
        req.setEffectiveDate(LocalDate.of(2026, 10, 1));
        req.setComments("Standard merit increment");

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        EligibilityEvaluationResponse eval = new EligibilityEvaluationResponse();
        eval.setEligible(true);
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(appraisal), eq(5.0), eq(true)))
                .thenReturn(eval);

        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> {
            IncrementRecommendation r = inv.getArgument(0);
            r.setId(1001L);
            return r;
        });

        IncrementRecommendationResponse resp = recommendationService.createRecommendation(req);

        assertNotNull(resp);
        assertEquals(1001L, resp.getId());
        assertEquals(125L, resp.getEmployeeId());
        assertEquals(new BigDecimal("50000.00"), resp.getCurrentSalary());
        assertEquals(new BigDecimal("5.0"), resp.getIncrementPercentage());
        assertEquals(new BigDecimal("2500.00"), resp.getIncrementAmount());
        assertEquals(new BigDecimal("52500.00"), resp.getRecommendedSalary());
        assertEquals(IncrementRecommendationStatus.RECOMMENDED, resp.getStatus());
        assertEquals("Standard merit increment", resp.getComments());

        // Verify cycle allocated budget updated
        assertEquals(new BigDecimal("2500.00"), cycle.getAllocatedBudget());
        verify(cycleRepository).save(cycle);
    }

    @Test
    @DisplayName("✅ 8% Increment: Current ₹50,000 -> Amount ₹4,000, New Salary ₹54,000")
    void testCreateRecommendation_8Percent_Success() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(8.0);
        req.setComments("Performance based increment");

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        EligibilityEvaluationResponse eval = new EligibilityEvaluationResponse();
        eval.setEligible(true);
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(appraisal), eq(8.0), eq(true)))
                .thenReturn(eval);

        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> {
            IncrementRecommendation r = inv.getArgument(0);
            r.setId(1002L);
            return r;
        });

        IncrementRecommendationResponse resp = recommendationService.createRecommendation(req);

        assertNotNull(resp);
        assertEquals(1002L, resp.getId());
        assertEquals(125L, resp.getEmployeeId());
        assertEquals(new BigDecimal("50000.00"), resp.getCurrentSalary());
        assertEquals(new BigDecimal("8.0"), resp.getIncrementPercentage());
        assertEquals(new BigDecimal("4000.00"), resp.getIncrementAmount());
        assertEquals(new BigDecimal("54000.00"), resp.getRecommendedSalary());
        assertEquals(IncrementRecommendationStatus.RECOMMENDED, resp.getStatus());
    }

    @Test
    @DisplayName("✅ Maximum Allowed % (20%): Current ₹50,000 -> Amount ₹10,000, New Salary ₹60,000")
    void testCreateRecommendation_MaxAllowedPercentage_Success() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(20.0); // Exactly policy max

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        EligibilityEvaluationResponse eval = new EligibilityEvaluationResponse();
        eval.setEligible(true);
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(appraisal), eq(20.0), eq(true)))
                .thenReturn(eval);

        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> {
            IncrementRecommendation r = inv.getArgument(0);
            r.setId(1003L);
            return r;
        });

        IncrementRecommendationResponse resp = recommendationService.createRecommendation(req);

        assertNotNull(resp);
        assertEquals(new BigDecimal("20.0"), resp.getIncrementPercentage());
        assertEquals(new BigDecimal("10000.00"), resp.getIncrementAmount());
        assertEquals(new BigDecimal("60000.00"), resp.getRecommendedSalary());
    }

    @Test
    @DisplayName(" Below Minimum % (2.0% < 3.0% min) -> Throws BadRequestException")
    void testCreateRecommendation_BelowMinPercentage_ThrowsBadRequest() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(2.0); // Below policy minimum of 3.0%

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("cannot be less than policy minimum"));
    }

    @Test
    @DisplayName(" Above Maximum % (25.0% > 20.0% max) -> Throws BadRequestException")
    void testCreateRecommendation_AboveMaxPercentage_ThrowsBadRequest() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(25.0); // Exceeds policy max of 20.0%

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("exceeds policy maximum"));
    }

    @Test
    @DisplayName(" Ineligible Employee -> Throws BadRequestException")
    void testCreateRecommendation_IneligibleEmployee_ThrowsBadRequest() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.empty());

        EligibilityEvaluationResponse eval = new EligibilityEvaluationResponse();
        eval.setEligible(false);
        eval.setReasons(List.of(new EligibilityEvaluationResponse.IneligibleReasonDto(
                "RATING", "3.0", "2.5", "Final rating 2.5 is below minimum required 3.0")));
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(appraisal), eq(8.0), eq(true)))
                .thenReturn(eval);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("Final rating 2.5 is below minimum required 3.0"));
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    @DisplayName(" Duplicate Recommendation for Employee in Cycle -> Throws BadRequestException")
    void testCreateRecommendation_DuplicateRecommendation_ThrowsBadRequest() {
        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));

        IncrementRecommendation existing = new IncrementRecommendation();
        existing.setId(99L);
        existing.setStatus(IncrementRecommendationStatus.RECOMMENDED);
        when(recommendationRepository.findActiveRecommendation(100L, 125L, 501L)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    @DisplayName(" Closed Cycle -> Throws BadRequestException")
    void testCreateRecommendation_ClosedCycle_ThrowsBadRequest() {
        cycle.setStatus(IncrementCycleStatus.CLOSED);

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("Cannot create recommendations for a cycle that is not OPEN"));
    }

    @Test
    @DisplayName(" Wrong Appraisal: Belongs to Different Employee -> Throws BadRequestException")
    void testCreateRecommendation_WrongAppraisal_EmployeeMismatch_ThrowsBadRequest() {
        Employee otherEmp = new Employee();
        otherEmp.setId(999L);
        appraisal.setEmployee(otherEmp); // Belongs to employee 999, not 125

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("does not belong to the specified employee"));
    }

    @Test
    @DisplayName(" Wrong Appraisal: Not Completed Status -> Throws BadRequestException")
    void testCreateRecommendation_WrongAppraisal_NotCompletedStatus_ThrowsBadRequest() {
        appraisal.setStatus(AppraisalStatus.SELF_ASSESSMENT); // Not finalized

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(appraisal));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("allowed only for finalized appraisals"));
    }

    @Test
    @DisplayName(" Inactive Employee -> Throws BadRequestException")
    void testCreateRecommendation_InactiveEmployee_ThrowsBadRequest() {
        employee.setStatus("INACTIVE");

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(125L);
        req.setIncrementPercentage(8.0);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(125L)).thenReturn(Optional.of(employee));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> recommendationService.createRecommendation(req));
        assertTrue(ex.getMessage().contains("inactive employee"));
    }

    @Test
    @DisplayName("✅ Update Recommendation: Recalculates amount, recommended salary and sets REVISED")
    void testUpdateRecommendation_Success() {
        IncrementRecommendation rec = new IncrementRecommendation();
        rec.setId(555L);
        rec.setOrganization(organization);
        rec.setCycle(cycle);
        rec.setEmployee(employee);
        rec.setCurrentSalary(new BigDecimal("50000.00"));
        rec.setIncrementPercentage(new BigDecimal("5.0"));
        rec.setIncrementAmount(new BigDecimal("2500.00"));
        rec.setRecommendedSalary(new BigDecimal("52500.00"));
        rec.setStatus(IncrementRecommendationStatus.RECOMMENDED);

        UpdateRecommendationRequest updateReq = new UpdateRecommendationRequest();
        updateReq.setIncrementPercentage(10.0);
        updateReq.setComments("Revised based on calibration meeting");

        when(recommendationRepository.findByIdAndOrgId(555L, 1L)).thenReturn(Optional.of(rec));
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        IncrementRecommendationResponse resp = recommendationService.updateRecommendation(555L, updateReq);

        assertNotNull(resp);
        assertEquals(new BigDecimal("10.0"), resp.getIncrementPercentage());
        assertEquals(new BigDecimal("5000.00"), resp.getIncrementAmount());
        assertEquals(new BigDecimal("55000.00"), resp.getRecommendedSalary());
        assertEquals(IncrementRecommendationStatus.REVISED, resp.getStatus());
        assertEquals("Revised based on calibration meeting", resp.getComments());
    }

    @Test
    @DisplayName("✅ Get Employee Increment History: Maps implemented history correctly")
    void testGetEmployeeIncrementHistory_Success() {
        IncrementRecommendation rec = new IncrementRecommendation();
        rec.setId(777L);
        rec.setCycle(cycle);
        rec.setEmployee(employee);
        rec.setCurrentSalary(new BigDecimal("50000.00"));
        rec.setIncrementPercentage(new BigDecimal("10.0"));
        rec.setIncrementAmount(new BigDecimal("5000.00"));
        rec.setRecommendedSalary(new BigDecimal("55000.00"));
        rec.setEffectiveDate(LocalDate.of(2026, 4, 1));
        rec.setStatus(IncrementRecommendationStatus.IMPLEMENTED);

        when(recommendationRepository.findImplementedHistoryByEmployeeIdAndOrgId(125L, 1L))
                .thenReturn(List.of(rec));

        EmployeeIncrementHistoryResponse history = recommendationService.getEmployeeIncrementHistory(125L);

        assertNotNull(history);
        assertEquals(125L, history.getEmployeeId());
        assertEquals(1, history.getHistory().size());
        EmployeeIncrementHistoryResponse.HistoryItemDto item = history.getHistory().get(0);
        assertEquals(777L, item.getRecommendationId());
        assertEquals(new BigDecimal("50000.00"), item.getPreviousSalary());
        assertEquals(new BigDecimal("55000.00"), item.getRevisedSalary());
        assertEquals("IMPLEMENTED", item.getStatus());
    }
}
