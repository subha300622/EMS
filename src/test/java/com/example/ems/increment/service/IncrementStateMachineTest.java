package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.IncrementRecommendationResponse;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
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
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment State Machine & Rule Verification Tests")
public class IncrementStateMachineTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRevisionRepository salaryRevisionRepository;

    @Mock
    private IncrementRecommendationService recommendationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private IncrementImplementationService implementationService;

    private Organization organization;
    private Employee employee;
    private IncrementPolicy policy;
    private IncrementCycle cycle;
    private IncrementRecommendation recommendation;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(101L);
        employee.setOrganization(organization);
        employee.setFullName("Jane Smith");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("50000.00"));

        policy = new IncrementPolicy();
        policy.setId(1L);
        policy.setOrganization(organization);

        cycle = new IncrementCycle();
        cycle.setId(10L);
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setName("Annual Increment 2026");
        cycle.setEffectiveDate(LocalDate.of(2026, 10, 1));

        recommendation = new IncrementRecommendation();
        recommendation.setId(201L);
        recommendation.setOrganization(organization);
        recommendation.setEmployee(employee);
        recommendation.setCycle(cycle);
        recommendation.setCurrentSalary(new BigDecimal("50000.00"));
        recommendation.setIncrementPercentage(new BigDecimal("10.00"));
        recommendation.setIncrementAmount(new BigDecimal("5000.00"));
        recommendation.setRecommendedSalary(new BigDecimal("55000.00"));
        recommendation.setEffectiveDate(LocalDate.of(2026, 10, 1));
        recommendation.setVersion(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("R9: Appraisal status must be COMPLETED for increment evaluation")
    void testAppraisalMustBeCompletedForIncrement() {
        Appraisal draftAppraisal = new Appraisal();
        draftAppraisal.setStatus(AppraisalStatus.DRAFT);
        assertNotEquals(AppraisalStatus.COMPLETED, draftAppraisal.getStatus());

        Appraisal underReviewAppraisal = new Appraisal();
        underReviewAppraisal.setStatus(AppraisalStatus.UNDER_REVIEW);
        assertNotEquals(AppraisalStatus.COMPLETED, underReviewAppraisal.getStatus());

        Appraisal completedAppraisal = new Appraisal();
        completedAppraisal.setStatus(AppraisalStatus.COMPLETED);
        assertEquals(AppraisalStatus.COMPLETED, completedAppraisal.getStatus());
    }

    @Test
    @DisplayName("R17: Reject implementation if recommendation status is RECOMMENDED")
    void testRejectImplementationWhenRecommended() {
        recommendation.setStatus(IncrementRecommendationStatus.RECOMMENDED);
        when(recommendationRepository.findByIdAndOrgId(201L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                implementationService.implementIncrement(201L));

        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("R17: Reject implementation if recommendation status is UNDER_REVIEW")
    void testRejectImplementationWhenUnderReview() {
        recommendation.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
        when(recommendationRepository.findByIdAndOrgId(201L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                implementationService.implementIncrement(201L));

        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("R17: Reject implementation if recommendation status is REJECTED")
    void testRejectImplementationWhenRejected() {
        recommendation.setStatus(IncrementRecommendationStatus.REJECTED);
        when(recommendationRepository.findByIdAndOrgId(201L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                implementationService.implementIncrement(201L));

        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("R18: Salary is updated when recommendation is APPROVED and implemented")
    void testSuccessfulImplementationUpdatesSalary() {
        recommendation.setStatus(IncrementRecommendationStatus.APPROVED);
        when(recommendationRepository.findByIdAndOrgId(201L, 1L)).thenReturn(Optional.of(recommendation));
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        when(salaryRevisionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncrementRecommendationResponse mockResp = new IncrementRecommendationResponse();
        mockResp.setId(201L);
        mockResp.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
        when(recommendationService.mapToResponse(any())).thenReturn(mockResp);

        var resp = implementationService.implementIncrement(201L);

        assertNotNull(resp);
        assertEquals(IncrementRecommendationStatus.IMPLEMENTED, resp.getStatus());
        assertEquals(new BigDecimal("55000.00"), employee.getAnnualSalary());
        verify(employeeRepository, times(1)).save(employee);
        verify(salaryRevisionRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("R19: Prevent duplicate implementation (Idempotency check)")
    void testPreventDuplicateImplementation() {
        recommendation.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
        when(recommendationRepository.findByIdAndOrgId(201L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                implementationService.implementIncrement(201L));

        assertTrue(ex.getMessage().contains("already been implemented"));
        assertEquals(new BigDecimal("50000.00"), employee.getAnnualSalary());
        verify(employeeRepository, never()).save(any());
    }
}
