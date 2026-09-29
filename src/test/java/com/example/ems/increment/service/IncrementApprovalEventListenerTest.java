package com.example.ems.increment.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment Approval Event Listener Unit Tests")
public class IncrementApprovalEventListenerTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @InjectMocks
    private IncrementApprovalEventListener eventListener;

    private Organization organization;
    private IncrementRecommendation recommendation;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);

        recommendation = new IncrementRecommendation();
        recommendation.setId(500L);
        recommendation.setOrganization(organization);
        recommendation.setApprovalRequestId(8800L);
        recommendation.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("✅ On ApprovalWorkflowCompletedEvent: Transitions to APPROVED")
    void testOnApprovalWorkflowCompleted_Success() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "8800",
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "500",
                1L,
                ApprovalStatus.APPROVED);

        when(recommendationRepository.findById(500L)).thenReturn(Optional.of(recommendation));

        eventListener.onApprovalWorkflowCompleted(event);

        assertEquals(IncrementRecommendationStatus.APPROVED, recommendation.getStatus());
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    @DisplayName("✅ On ApprovalWorkflowRejectedEvent: Transitions to REJECTED with reason")
    void testOnApprovalWorkflowRejected_Success() {
        ApprovalWorkflowRejectedEvent event = new ApprovalWorkflowRejectedEvent(
                this,
                "8800",
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "500",
                1L,
                "Budget threshold reached for this cycle");

        when(recommendationRepository.findById(500L)).thenReturn(Optional.of(recommendation));

        eventListener.onApprovalWorkflowRejected(event);

        assertEquals(IncrementRecommendationStatus.REJECTED, recommendation.getStatus());
        assertEquals("Budget threshold reached for this cycle", recommendation.getRejectionReason());
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    @DisplayName("✅ On ApprovalChangesRequestedEvent: Transitions to SENT_BACK with comments")
    void testOnApprovalChangesRequested_Success() {
        ApprovalChangesRequestedEvent event = new ApprovalChangesRequestedEvent(
                this,
                "8800",
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "500",
                1L,
                "Please reduce percentage to 6.0%");

        when(recommendationRepository.findById(500L)).thenReturn(Optional.of(recommendation));

        eventListener.onApprovalChangesRequested(event);

        assertEquals(IncrementRecommendationStatus.SENT_BACK, recommendation.getStatus());
        assertEquals("Please reduce percentage to 6.0%", recommendation.getRejectionReason());
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    @DisplayName(" Unrelated workflow type ignored (e.g. LEAVE_APPROVAL)")
    void testOnApprovalWorkflowCompleted_UnrelatedWorkflowType_Ignored() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "9900",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE",
                "500",
                1L,
                ApprovalStatus.APPROVED);

        eventListener.onApprovalWorkflowCompleted(event);

        verify(recommendationRepository, never()).findById(any());
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    @DisplayName(" Unknown recommendation ID ignored safely")
    void testOnApprovalWorkflowCompleted_UnknownRecommendation_Ignored() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "8800",
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "999",
                1L,
                ApprovalStatus.APPROVED);

        when(recommendationRepository.findById(999L)).thenReturn(Optional.empty());

        eventListener.onApprovalWorkflowCompleted(event);

        verify(recommendationRepository, never()).save(any());
    }

    @Test
    @DisplayName(" Organization mismatch ignored safely")
    void testOnApprovalWorkflowCompleted_WrongTenant_Ignored() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "8800",
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "500",
                2L, // Organization 2, while entity belongs to Organization 1
                ApprovalStatus.APPROVED);

        when(recommendationRepository.findById(500L)).thenReturn(Optional.of(recommendation));

        eventListener.onApprovalWorkflowCompleted(event);

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, recommendation.getStatus());
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    @DisplayName(" Workflow instance ID mismatch ignored safely")
    void testOnApprovalWorkflowCompleted_WrongWorkflowInstanceId_Ignored() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "9999", // Different workflow ID from entity's 8800L
                WorkflowType.INCREMENT_RECOMMENDATION,
                "INCREMENT_RECOMMENDATION",
                "500",
                1L,
                ApprovalStatus.APPROVED);

        when(recommendationRepository.findById(500L)).thenReturn(Optional.of(recommendation));

        eventListener.onApprovalWorkflowCompleted(event);

        assertEquals(IncrementRecommendationStatus.UNDER_REVIEW, recommendation.getStatus());
        verify(recommendationRepository, never()).save(any());
    }
}
