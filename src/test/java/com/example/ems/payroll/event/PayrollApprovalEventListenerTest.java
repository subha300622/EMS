package com.example.ems.payroll.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;
import com.example.ems.payroll.repository.PayrollRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollApprovalEventListenerTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @InjectMocks
    private PayrollApprovalEventListener listener;

    private PayrollRun run;

    @BeforeEach
    void setUp() {
        run = new PayrollRun(10L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(100L);
        run.setStatus(PayrollRunStatus.PENDING_APPROVAL);
        run.setApprovalInstanceId("WF-INST-100");
    }

    @Test
    @DisplayName("Completed Event: Transition status from PENDING_APPROVAL to APPROVED")
    void testApprovalCompleted() {
        when(payrollRunRepository.findById(100L)).thenReturn(Optional.of(run));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-INST-100", WorkflowType.PAYROLL_APPROVAL, "PAYROLL", "100", 10L, ApprovalStatus.APPROVED
        );

        listener.onApprovalWorkflowCompleted(event);

        assertEquals(PayrollRunStatus.APPROVED, run.getStatus());
        assertNotNull(run.getApprovedAt());
        assertNull(run.getRejectionReason());
        verify(payrollRunRepository, times(1)).save(run);
    }

    @Test
    @DisplayName("Completed Event: Idempotency - duplicate event does not mutate already approved run")
    void testApprovalCompleted_Idempotent() {
        run.setStatus(PayrollRunStatus.APPROVED);
        when(payrollRunRepository.findById(100L)).thenReturn(Optional.of(run));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-INST-100", WorkflowType.PAYROLL_APPROVAL, "PAYROLL", "100", 10L, ApprovalStatus.APPROVED
        );

        listener.onApprovalWorkflowCompleted(event);

        verify(payrollRunRepository, never()).save(any());
    }

    @Test
    @DisplayName("Completed Event: Ignore unrelated workflow type")
    void testApprovalCompleted_UnrelatedWorkflow() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-INST-100", WorkflowType.LEAVE_APPROVAL, "LEAVE", "100", 10L, ApprovalStatus.APPROVED
        );

        listener.onApprovalWorkflowCompleted(event);

        verify(payrollRunRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Completed Event: Tenant mismatch ignored")
    void testApprovalCompleted_TenantMismatch() {
        when(payrollRunRepository.findById(100L)).thenReturn(Optional.of(run));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-INST-100", WorkflowType.PAYROLL_APPROVAL, "PAYROLL", "100", 999L, ApprovalStatus.APPROVED
        );

        listener.onApprovalWorkflowCompleted(event);

        assertEquals(PayrollRunStatus.PENDING_APPROVAL, run.getStatus());
        verify(payrollRunRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejected Event: Transition status to REJECTED with reason")
    void testApprovalRejected() {
        when(payrollRunRepository.findById(100L)).thenReturn(Optional.of(run));

        ApprovalWorkflowRejectedEvent event = new ApprovalWorkflowRejectedEvent(
                this, "WF-INST-100", WorkflowType.PAYROLL_APPROVAL, "PAYROLL", "100", 10L, "Budget exceeded for Q3"
        );

        listener.onApprovalWorkflowRejected(event);

        assertEquals(PayrollRunStatus.REJECTED, run.getStatus());
        assertEquals("Budget exceeded for Q3", run.getRejectionReason());
        verify(payrollRunRepository, times(1)).save(run);
    }

    @Test
    @DisplayName("Changes Requested Event: Transition status to CHANGES_REQUESTED with comments")
    void testApprovalChangesRequested() {
        when(payrollRunRepository.findById(100L)).thenReturn(Optional.of(run));

        ApprovalChangesRequestedEvent event = new ApprovalChangesRequestedEvent(
                this, "WF-INST-100", WorkflowType.PAYROLL_APPROVAL, "PAYROLL", "100", 10L, "Please adjust OT hours for team A"
        );

        listener.onApprovalChangesRequested(event);

        assertEquals(PayrollRunStatus.CHANGES_REQUESTED, run.getStatus());
        assertEquals("Please adjust OT hours for team A", run.getRejectionReason());
        verify(payrollRunRepository, times(1)).save(run);
    }
}
