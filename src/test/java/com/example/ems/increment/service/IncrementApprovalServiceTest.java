package com.example.ems.increment.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.dto.SubmitApprovalResponse;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementCycleStatus;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment Approval Service Unit Tests")
public class IncrementApprovalServiceTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private IncrementApprovalService approvalService;

    private Organization organization;
    private IncrementCycle cycle;
    private Employee employee;
    private IncrementRecommendation recommendation;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setOrganization(organization);

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setName("Annual Cycle 2026");
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setStatus(IncrementCycleStatus.OPEN);

        employee = new Employee();
        employee.setId(125L);
        employee.setEmployeeId("EMP125");
        employee.setFullName("John Doe");
        employee.setOrganization(organization);

        recommendation = new IncrementRecommendation();
        recommendation.setId(500L);
        recommendation.setOrganization(organization);
        recommendation.setCycle(cycle);
        recommendation.setEmployee(employee);
        recommendation.setCurrentSalary(new BigDecimal("50000.00"));
        recommendation.setIncrementPercentage(new BigDecimal("8.0"));
        recommendation.setIncrementAmount(new BigDecimal("4000.00"));
        recommendation.setRecommendedSalary(new BigDecimal("54000.00"));
        recommendation.setEffectiveDate(LocalDate.of(2026, 10, 1));
        recommendation.setStatus(IncrementRecommendationStatus.RECOMMENDED);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Submit Recommendation: RECOMMENDED -> UNDER_REVIEW with ApprovalWorkflowInstance")
    void testSubmitRecommendation_Success() {
        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setId(8800L);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(instance);
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitApprovalResponse response = approvalService.submitForApproval(500L);

        assertNotNull(response);
        assertEquals(500L, response.getRecommendationId());
        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals(8800L, response.getApprovalRequestId());

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, recommendation.getStatus());
        assertEquals(8800L, recommendation.getApprovalRequestId());

        ArgumentCaptor<ApprovalContext> contextCaptor = ArgumentCaptor.forClass(ApprovalContext.class);
        verify(approvalFacade).startApproval(contextCaptor.capture());
        ApprovalContext captured = contextCaptor.getValue();
        assertEquals("INCREMENT_RECOMMENDATION", captured.getModule());
        assertEquals("500", captured.getResourceId());
        assertEquals("EMP125", captured.getEmployeeId());
        assertEquals(new BigDecimal("4000.00"), captured.getAmount());
    }

    @Test
    @DisplayName("Submit Recommendation from REVISED status -> UNDER_REVIEW")
    void testSubmitRecommendation_FromRevisedStatus_Success() {
        recommendation.setStatus(IncrementRecommendationStatus.REVISED);

        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setId(8801L);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(instance);
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitApprovalResponse response = approvalService.submitForApproval(500L);

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals(8801L, response.getApprovalRequestId());
    }

    @Test
    @DisplayName("Submit Recommendation from SENT_BACK status -> UNDER_REVIEW")
    void testSubmitRecommendation_FromSentBackStatus_Success() {
        recommendation.setStatus(IncrementRecommendationStatus.SENT_BACK);

        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setId(8802L);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(instance);
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitApprovalResponse response = approvalService.submitForApproval(500L);

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals(8802L, response.getApprovalRequestId());
    }

    @Test
    @DisplayName("Submit already UNDER_REVIEW recommendation -> Throws BadRequestException")
    void testSubmitRecommendation_AlreadyUnderReview_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
        recommendation.setApprovalRequestId(8800L);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> approvalService.submitForApproval(500L));
        assertTrue(ex.getMessage().contains("already UNDER_REVIEW"));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit already APPROVED recommendation -> Throws BadRequestException")
    void testSubmitRecommendation_AlreadyApproved_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.APPROVED);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> approvalService.submitForApproval(500L));
        assertTrue(ex.getMessage().contains("already APPROVED"));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit already IMPLEMENTED recommendation -> Throws BadRequestException")
    void testSubmitRecommendation_AlreadyImplemented_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.IMPLEMENTED);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> approvalService.submitForApproval(500L));
        assertTrue(ex.getMessage().contains("already IMPLEMENTED"));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit REJECTED recommendation -> Throws BadRequestException")
    void testSubmitRecommendation_RejectedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.REJECTED);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> approvalService.submitForApproval(500L));
        assertTrue(ex.getMessage().contains("Cannot submit a REJECTED recommendation"));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit recommendation for CLOSED cycle -> Throws BadRequestException")
    void testSubmitRecommendation_ClosedCycle_ThrowsBadRequest() {
        cycle.setStatus(IncrementCycleStatus.CLOSED);

        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> approvalService.submitForApproval(500L));
        assertTrue(ex.getMessage().contains("cycle is not OPEN"));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit recommendation for wrong tenant -> Throws ResourceNotFoundException")
    void testSubmitRecommendation_WrongTenant_ThrowsResourceNotFound() {
        when(recommendationRepository.findByIdAndOrgId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> approvalService.submitForApproval(999L));
        verify(approvalFacade, never()).startApproval(any());
    }

    @Test
    @DisplayName("Submit recommendation fallback when approvalFacade encounters exception")
    void testSubmitRecommendation_ApprovalFacadeException_UsesFallback() {
        when(recommendationRepository.findByIdAndOrgId(500L, 1L)).thenReturn(Optional.of(recommendation));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenThrow(new RuntimeException("Workflow template not found"));
        when(recommendationRepository.save(any(IncrementRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitApprovalResponse response = approvalService.submitForApproval(500L);

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals(7500L, response.getApprovalRequestId()); // 500 + 7000 fallback
    }
}
