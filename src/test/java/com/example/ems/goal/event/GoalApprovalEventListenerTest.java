package com.example.ems.goal.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.goal.service.GoalActivityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalApprovalEventListenerTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalActivityService activityService;

    @InjectMocks
    private GoalApprovalEventListener eventListener;

    @Test
    @DisplayName("Scenario 1: Goal Creation Approval APPROVED -> Goal status becomes ACTIVE")
    void testGoalCreationApproved() {
        Goal goal = new Goal();
        goal.setId(100L);
        goal.setStatus("PENDING_APPROVAL");

        when(goalRepository.findById(100L)).thenReturn(Optional.of(goal));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-101",
                WorkflowType.GOAL_CREATION_APPROVAL,
                "GOAL",
                "100",
                1L,
                ApprovalStatus.APPROVED
        );

        eventListener.handleApprovalCompleted(event);

        assertEquals("ACTIVE", goal.getStatus());
        verify(goalRepository).save(goal);
        verify(activityService).logActivity(eq(100L), any(), eq("Approval Engine"), eq("SYSTEM"), eq("GOAL_APPROVED"), anyString(), any());
    }

    @Test
    @DisplayName("Scenario 2: Goal Creation Approval REJECTED -> Goal status reverts to DRAFT")
    void testGoalCreationRejected() {
        Goal goal = new Goal();
        goal.setId(200L);
        goal.setStatus("PENDING_APPROVAL");

        when(goalRepository.findById(200L)).thenReturn(Optional.of(goal));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-102",
                WorkflowType.GOAL_CREATION_APPROVAL,
                "GOAL",
                "200",
                1L,
                ApprovalStatus.REJECTED
        );

        eventListener.handleApprovalCompleted(event);

        assertEquals("DRAFT", goal.getStatus());
        verify(goalRepository).save(goal);
        verify(activityService).logActivity(eq(200L), any(), eq("Approval Engine"), eq("SYSTEM"), eq("GOAL_REJECTED"), anyString(), any());
    }

    @Test
    @DisplayName("Scenario 4: Goal Completion Approval APPROVED -> Goal status becomes COMPLETED with 100% progress")
    void testGoalCompletionApproved() {
        Goal goal = new Goal();
        goal.setId(300L);
        goal.setStatus("PENDING_APPROVAL");
        goal.setProgress(90);

        when(goalRepository.findById(300L)).thenReturn(Optional.of(goal));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-103",
                WorkflowType.GOAL_COMPLETION_APPROVAL,
                "GOAL",
                "300",
                1L,
                ApprovalStatus.APPROVED
        );

        eventListener.handleApprovalCompleted(event);

        assertEquals("COMPLETED", goal.getStatus());
        assertEquals(100, goal.getProgress());
        verify(goalRepository).save(goal);
        verify(activityService).logActivity(eq(300L), any(), eq("Approval Engine"), eq("SYSTEM"), eq("GOAL_COMPLETED"), anyString(), any());
    }

    @Test
    @DisplayName("Scenario 5: Goal Completion Approval REJECTED -> Goal status reverts to DRAFT")
    void testGoalCompletionRejected() {
        Goal goal = new Goal();
        goal.setId(400L);
        goal.setStatus("PENDING_APPROVAL");

        when(goalRepository.findById(400L)).thenReturn(Optional.of(goal));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-104",
                WorkflowType.GOAL_COMPLETION_APPROVAL,
                "GOAL",
                "400",
                1L,
                ApprovalStatus.REJECTED
        );

        eventListener.handleApprovalCompleted(event);

        assertEquals("DRAFT", goal.getStatus());
        verify(goalRepository).save(goal);
        verify(activityService).logActivity(eq(400L), any(), eq("Approval Engine"), eq("SYSTEM"), eq("GOAL_REJECTED"), anyString(), any());
    }
}
