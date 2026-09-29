package com.example.ems.incentive.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Listens for Central Approval Platform completion events and updates the Incentive record accordingly.
 */
@Component
public class IncentiveApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(IncentiveApprovalEventListener.class);

    private final IncentiveRecordRepository recordRepository;

    public IncentiveApprovalEventListener(IncentiveRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) {
            return;
        }

        if (event.getWorkflowType() != WorkflowType.INCENTIVE_REQUEST) {
            return;
        }

        String refId = event.getBusinessReferenceId();
        if (refId == null || refId.isBlank()) {
            return;
        }

        try {
            Long recordId = Long.parseLong(refId.trim());
            IncentiveRecord record = recordRepository.findById(recordId).orElse(null);
            if (record == null) {
                log.warn("IncentiveRecord not found for completed workflow event: recordId={}", recordId);
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                log.info("IncentiveRecord ID={} received FINAL approval from Central Approval Platform.", recordId);
                BigDecimal effectiveAmount = record.getAdjustedAmount() != null
                        ? record.getAdjustedAmount()
                        : record.getCalculatedAmount();

                record.setApprovedAmount(effectiveAmount);
                record.setStatus(IncentiveStatus.APPROVED);
                record.setApprovedBy("CENTRAL_APPROVAL_ENGINE");
                record.setApprovedAt(LocalDateTime.now());
                record.setPayrollStatus(IncentivePayrollStatus.PENDING);

                recordRepository.save(record);
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                log.info("IncentiveRecord ID={} was REJECTED by Central Approval Platform.", recordId);
                record.setStatus(IncentiveStatus.REJECTED);
                record.setRejectionReason("Rejected via central approval workflow");
                record.setRejectedBy("CENTRAL_APPROVAL_ENGINE");
                record.setRejectedAt(LocalDateTime.now());

                recordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Failed to process approval workflow completion for incentive record event: {}", event, e);
        }
    }
}
