package com.example.ems.incentive.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncentiveApprovalEventListenerTest {

    @Mock
    private IncentiveRecordRepository recordRepository;

    @InjectMocks
    private IncentiveApprovalEventListener listener;

    @Test
    void onApprovalWorkflowCompleted_notIncentiveWorkflow_ignored() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "wf-1", WorkflowType.LEAVE_APPROVAL, "LEAVE_REQUEST", "100", 1L, ApprovalStatus.APPROVED
        );

        listener.onApprovalWorkflowCompleted(event);

        verifyNoInteractions(recordRepository);
    }

    @Test
    void onApprovalWorkflowCompleted_approved_updatesRecordToApproved() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "wf-2", WorkflowType.INCENTIVE_REQUEST, "INCENTIVE_RECORD", "200", 1L, ApprovalStatus.APPROVED
        );

        IncentiveRecord record = IncentiveRecord.builder()
                .id(200L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .adjustedAmount(new BigDecimal("4500.00"))
                .status(IncentiveStatus.PENDING_APPROVAL)
                .build();

        when(recordRepository.findById(200L)).thenReturn(Optional.of(record));

        listener.onApprovalWorkflowCompleted(event);

        assertThat(record.getStatus()).isEqualTo(IncentiveStatus.APPROVED);
        assertThat(record.getApprovedAmount()).isEqualByComparingTo("4500.00");
        assertThat(record.getPayrollStatus()).isEqualTo(IncentivePayrollStatus.PENDING);
        assertThat(record.getApprovedBy()).isEqualTo("CENTRAL_APPROVAL_ENGINE");
        verify(recordRepository).save(record);
    }

    @Test
    void onApprovalWorkflowCompleted_rejected_updatesRecordToRejected() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "wf-3", WorkflowType.INCENTIVE_REQUEST, "INCENTIVE_RECORD", "200", 1L, ApprovalStatus.REJECTED
        );

        IncentiveRecord record = IncentiveRecord.builder()
                .id(200L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .status(IncentiveStatus.PENDING_APPROVAL)
                .build();

        when(recordRepository.findById(200L)).thenReturn(Optional.of(record));

        listener.onApprovalWorkflowCompleted(event);

        assertThat(record.getStatus()).isEqualTo(IncentiveStatus.REJECTED);
        assertThat(record.getRejectionReason()).isNotEmpty();
        assertThat(record.getRejectedBy()).isEqualTo("CENTRAL_APPROVAL_ENGINE");
        verify(recordRepository).save(record);
    }
}
