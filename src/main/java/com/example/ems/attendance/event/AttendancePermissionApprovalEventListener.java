package com.example.ems.attendance.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.attendance.entity.AttendancePermission;
import com.example.ems.attendance.entity.AttendancePermissionStatus;
import com.example.ems.attendance.repository.AttendancePermissionRepository;
import com.example.ems.attendance.service.AttendancePermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Event listener that processes final approval / rejection events for attendance permissions.
 * Guaranteed Rule: Attendance adjustments are automatically applied when the central workflow reaches final APPROVED state.
 */
@Component
public class AttendancePermissionApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(AttendancePermissionApprovalEventListener.class);

    @Autowired
    private AttendancePermissionRepository permissionRepository;

    @Autowired
    private AttendancePermissionService permissionService;

    @EventListener
    @Transactional
    public void handleWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) {
            return;
        }

        WorkflowType type = event.getWorkflowType();
        if (type != WorkflowType.ATTENDANCE_PERMISSION && type != WorkflowType.ATTENDANCE_PERMISSION_APPROVAL) {
            return;
        }

        String businessRefId = event.getBusinessReferenceId();
        if (businessRefId == null || businessRefId.isBlank()) {
            return;
        }

        try {
            Long permissionId = Long.parseLong(businessRefId.trim());
            AttendancePermission permission = permissionRepository.findById(permissionId).orElse(null);
            if (permission == null) {
                log.warn("AttendancePermission not found for completed workflow event: permissionId={}", permissionId);
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                log.info("Attendance permission {} received FINAL approval. Applying attendance adjustment.", permissionId);
                permission.setStatus(AttendancePermissionStatus.APPROVED);
                permission.setApprovedAt(Instant.now());
                if (permission.getApprovedBy() == null) {
                    permission.setApprovedBy("WORKFLOW_ENGINE");
                }
                permission = permissionRepository.save(permission);

                // Apply attendance adjustment
                permissionService.applyPermissionToAttendance(permission);

            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                log.info("Attendance permission {} was REJECTED by workflow engine.", permissionId);
                permission.setStatus(AttendancePermissionStatus.REJECTED);
                if (permission.getRejectionReason() == null) {
                    permission.setRejectionReason("Rejected via central approval workflow");
                }
                permissionRepository.save(permission);
            }
        } catch (Exception e) {
            log.error("Failed to process approval workflow completion for attendance permission event: {}", event, e);
        }
    }
}
