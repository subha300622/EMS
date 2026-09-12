package com.example.ems.bonus.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Listens for Central Approval Platform completion events and updates the Bonus record accordingly.
 */
@Component
public class BonusApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(BonusApprovalEventListener.class);

    private final BonusRecordRepository recordRepository;

    public BonusApprovalEventListener(BonusRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) {
            return;
        }

        if (event.getWorkflowType() != WorkflowType.BONUS_REQUEST) {
            return;
        }

        String refId = event.getBusinessReferenceId();
        if (refId == null || refId.isBlank()) {
            return;
        }

        try {
            Long recordId = Long.parseLong(refId.trim());
            BonusRecord record = recordRepository.findById(recordId).orElse(null);
            if (record == null) {
                log.warn("BonusRecord not found for completed workflow event: recordId={}", recordId);
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                log.info("BonusRecord ID={} received FINAL approval from Central Approval Platform.", recordId);
                BigDecimal effectiveAmount = record.getAdjustedAmount() != null
                        ? record.getAdjustedAmount()
                        : record.getCalculatedAmount();

                record.setApprovedAmount(effectiveAmount);
                record.setStatus(BonusStatus.APPROVED);
                record.setApprovedBy("CENTRAL_APPROVAL_ENGINE");
                record.setApprovedAt(LocalDateTime.now());
                record.setPayrollStatus(BonusPayrollStatus.PENDING);

                recordRepository.save(record);
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                log.info("BonusRecord ID={} was REJECTED by Central Approval Platform.", recordId);
                record.setStatus(BonusStatus.REJECTED);
                record.setRejectionReason("Rejected via central approval workflow");
                record.setRejectedBy("CENTRAL_APPROVAL_ENGINE");
                record.setRejectedAt(LocalDateTime.now());

                recordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Failed to process approval workflow completion for bonus record event: {}", event, e);
        }
    }
}
