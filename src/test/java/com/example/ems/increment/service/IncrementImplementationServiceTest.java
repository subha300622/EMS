package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.SalaryRevision;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.IncrementRecommendationResponse;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.event.SalaryRevisionCreatedEvent;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment Implementation & Salary Revision Unit Tests")
public class IncrementImplementationServiceTest {

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
    private IncrementCycle cycle;
    private IncrementRecommendation recommendation;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(125L);
        employee.setEmployeeId("EMP125");
        employee.setFullName("John Doe");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("600000.00"));

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setName("Annual Cycle 2026-27");

        recommendation = new IncrementRecommendation();
        recommendation.setId(123L);
        recommendation.setOrganization(organization);
        recommendation.setEmployee(employee);
        recommendation.setCycle(cycle);
        recommendation.setCurrentSalary(new BigDecimal("600000.00"));
        recommendation.setIncrementPercentage(new BigDecimal("10.0"));
        recommendation.setIncrementAmount(new BigDecimal("60000.00"));
        recommendation.setRecommendedSalary(new BigDecimal("660000.00"));
        recommendation.setEffectiveDate(LocalDate.of(2026, 10, 1));
        recommendation.setStatus(IncrementRecommendationStatus.APPROVED);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("APPROVED recommendation implemented: SalaryRevision created, Employee CTC updated, event published")
    void testImplementIncrement_Success() {
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));
        when(salaryRevisionRepository.save(any(SalaryRevision.class))).thenAnswer(inv -> {
            SalaryRevision sr = inv.getArgument(0);
            sr.setId(301L);
            return sr;
        });
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        IncrementRecommendationResponse mockResp = new IncrementRecommendationResponse();
        mockResp.setId(123L);
        mockResp.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
        mockResp.setCurrentSalary(new BigDecimal("600000.00"));
        mockResp.setRecommendedSalary(new BigDecimal("660000.00"));
        when(recommendationService.mapToResponse(any())).thenReturn(mockResp);

        IncrementRecommendationResponse result = implementationService.implementIncrement(123L);

        assertNotNull(result);
        assertEquals(IncrementRecommendationStatus.IMPLEMENTED, result.getStatus());
        assertEquals(IncrementRecommendationStatus.IMPLEMENTED, recommendation.getStatus());

        // Verify Employee compensation updated
        assertEquals(new BigDecimal("660000.00"), employee.getAnnualSalary());
        verify(employeeRepository).save(employee);

        // Verify SalaryRevision created with correct values
        ArgumentCaptor<SalaryRevision> revCaptor = ArgumentCaptor.forClass(SalaryRevision.class);
        verify(salaryRevisionRepository).save(revCaptor.capture());
        SalaryRevision capturedRevision = revCaptor.getValue();
        assertEquals(employee, capturedRevision.getEmployee());
        assertEquals(new BigDecimal("600000.00"), capturedRevision.getPreviousSalary());
        assertEquals(new BigDecimal("660000.00"), capturedRevision.getNewSalary());
        assertEquals(new BigDecimal("10.0"), capturedRevision.getChangePercentage());
        assertEquals(LocalDate.of(2026, 10, 1), capturedRevision.getEffectiveDate());
        assertTrue(capturedRevision.getReason().contains("Annual Cycle 2026-27"));

        // Verify SalaryRevisionCreatedEvent published
        ArgumentCaptor<SalaryRevisionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(SalaryRevisionCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SalaryRevisionCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrganizationId());
        assertEquals(125L, capturedEvent.getEmployeeId());
        assertEquals(123L, capturedEvent.getRecommendationId());
        assertEquals(301L, capturedEvent.getSalaryRevisionId());
        assertEquals(new BigDecimal("600000.00"), capturedEvent.getPreviousSalary());
        assertEquals(new BigDecimal("660000.00"), capturedEvent.getNewSalary());
        assertEquals(new BigDecimal("60000.00"), capturedEvent.getIncrementAmount());
    }

    @Test
    @DisplayName("RECOMMENDED cannot implement -> Throws BadRequestException")
    void testImplementIncrement_RecommendedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.RECOMMENDED);
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("Only APPROVED recommendations can be implemented"));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("UNDER_REVIEW cannot implement -> Throws BadRequestException")
    void testImplementIncrement_UnderReviewStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("Only APPROVED recommendations can be implemented"));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("REJECTED cannot implement -> Throws BadRequestException")
    void testImplementIncrement_RejectedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.REJECTED);
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("Only APPROVED recommendations can be implemented"));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("SENT_BACK cannot implement -> Throws BadRequestException")
    void testImplementIncrement_SentBackStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.SENT_BACK);
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("Only APPROVED recommendations can be implemented"));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("IMPLEMENTED cannot implement again (Idempotency) -> Throws BadRequestException")
    void testImplementIncrement_AlreadyImplemented_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("already been implemented"));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Concurrent implementation prevented via Optimistic Locking -> Throws OptimisticLockingFailureException")
    void testImplementIncrement_ConcurrentImplementation_ThrowsOptimisticLockingFailure() {
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));
        when(salaryRevisionRepository.save(any(SalaryRevision.class))).thenAnswer(inv -> inv.getArgument(0));
        when(recommendationRepository.save(any(IncrementRecommendation.class)))
                .thenThrow(new OptimisticLockingFailureException("Row was updated or deleted by another transaction"));

        assertThrows(OptimisticLockingFailureException.class, () ->
                implementationService.implementIncrement(123L)
        );
    }

    @Test
    @DisplayName("Wrong tenant rejected -> Throws ResourceNotFoundException")
    void testImplementIncrement_WrongTenant_ThrowsResourceNotFound() {
        when(recommendationRepository.findByIdAndOrgId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> implementationService.implementIncrement(999L));
        verify(employeeRepository, never()).save(any());
        verify(salaryRevisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inactive employee rejected -> Throws BadRequestException")
    void testImplementIncrement_InactiveEmployee_ThrowsBadRequest() {
        employee.setStatus("INACTIVE");
        when(recommendationRepository.findByIdAndOrgId(123L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> implementationService.implementIncrement(123L));
        assertTrue(ex.getMessage().contains("Employee is inactive or not found"));
        verify(salaryRevisionRepository, never()).save(any());
    }
}
