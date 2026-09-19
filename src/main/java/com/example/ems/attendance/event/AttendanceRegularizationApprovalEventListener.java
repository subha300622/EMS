package com.example.ems.attendance.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Event listener that processes final approval / rejection events for attendance regularizations.
 * Guaranteed Rule: Attendance records are only modified when the central workflow reaches final APPROVED state.
 */
@Component
public class AttendanceRegularizationApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRegularizationApprovalEventListener.class);

    @Autowired
    private AttendanceRegularizationRepository regularizationRepository;

    @Autowired
    private AttendanceService attendanceService;

    @EventListener
    @Transactional
    public void handleWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) {
            return;
        }

        WorkflowType type = event.getWorkflowType();
        if (type != WorkflowType.ATTENDANCE_REGULARIZATION && type != WorkflowType.ATTENDANCE_REGULARIZATION_APPROVAL) {
            return;
        }

        String businessRefId = event.getBusinessReferenceId();
        if (businessRefId == null || businessRefId.isBlank()) {
            return;
        }

        try {
            Long regId = Long.parseLong(businessRefId.trim());
            AttendanceRegularization reg = regularizationRepository.findById(regId).orElse(null);
            if (reg == null) {
                log.warn("Regularization not found for completed workflow event: regId={}", regId);
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                log.info("Attendance regularization {} received FINAL approval. Applying correction.", regId);
                reg.setStatus(AttendanceRegularizationStatus.APPROVED);
                reg.setApprovedAt(Instant.now());
                if (reg.getApprovedBy() == null) {
                    reg.setApprovedBy("WORKFLOW_ENGINE");
                }
                regularizationRepository.save(reg);

                // Centralized domain recalculation
                if (reg.getAttendance() != null) {
                    attendanceService.applyRegularizationCorrection(
                            reg.getAttendance().getId(),
                            reg.getRequestedCheckInTime(),
                            reg.getRequestedCheckOutTime()
                    );
                }
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                log.info("Attendance regularization {} was REJECTED by workflow engine. Attendance remains unchanged.", regId);
                reg.setStatus(AttendanceRegularizationStatus.REJECTED);
                if (reg.getRejectionReason() == null) {
                    reg.setRejectionReason("Rejected via central approval workflow");
                }
                regularizationRepository.save(reg);
            }
        } catch (Exception e) {
            log.error("Failed to process approval workflow completion for regularization event: {}", event, e);
        }
    }
}
