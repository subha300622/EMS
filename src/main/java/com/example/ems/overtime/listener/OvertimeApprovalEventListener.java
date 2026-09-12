package com.example.ems.overtime.listener;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Listens for Central Approval Platform completion events and updates the Overtime record accordingly.
 */
@Component
public class OvertimeApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(OvertimeApprovalEventListener.class);

    private final OvertimeRecordRepository recordRepository;

    public OvertimeApprovalEventListener(OvertimeRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) {
            return;
        }

        if (event.getWorkflowType() != WorkflowType.OVERTIME_REQUEST) {
            return;
        }

        String refId = event.getBusinessReferenceId();
        if (refId == null || refId.isBlank()) {
            return;
        }

        try {
            Long recordId = Long.parseLong(refId.trim());
            OvertimeRecord record = recordRepository.findById(recordId).orElse(null);
            if (record == null) {
                log.warn("OvertimeRecord not found for completed workflow event: recordId={}", recordId);
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                log.info("OvertimeRecord ID={} received FINAL approval from Central Approval Platform.", recordId);
                int effectiveMins = record.getAdjustedOtMinutes() != null ? record.getAdjustedOtMinutes() : record.getCalculatedOtMinutes();
                BigDecimal effectiveAmount = record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount();

                record.setApprovedOtMinutes(effectiveMins);
                record.setApprovedAmount(effectiveAmount);
                record.setStatus(OvertimeStatus.APPROVED);
                record.setApprovedBy("CENTRAL_APPROVAL_ENGINE");
                record.setApprovedAt(LocalDateTime.now());
                record.setPayrollStatus(OvertimePayrollStatus.PENDING);

                recordRepository.save(record);
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                log.info("OvertimeRecord ID={} was REJECTED by Central Approval Platform.", recordId);
                record.setStatus(OvertimeStatus.REJECTED);
                record.setRejectionReason("Rejected via central approval workflow");
                record.setRejectedBy("CENTRAL_APPROVAL_ENGINE");
                record.setRejectedAt(LocalDateTime.now());

                recordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Failed to process approval workflow completion for overtime record event: {}", event, e);
        }
    }
}
