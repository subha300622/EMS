package com.example.ems.leave.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveRequestHistory;
import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.event.LeaveRejectedEvent;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveRequestHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class LeaveApprovalEventListener {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceService balanceService;

    @Autowired
    private LeaveRequestHistoryRepository historyRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handleApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event.getWorkflowType() != WorkflowType.LEAVE_APPROVAL) {
            return;
        }

        String leaveIdStr = event.getBusinessReferenceId();
        if (leaveIdStr == null) return;

        Long leaveId;
        try {
            leaveId = Long.parseLong(leaveIdStr);
        } catch (NumberFormatException e) {
            return;
        }

        Leave leave = leaveRepository.findById(leaveId).orElse(null);
        if (leave == null) return;

        // Idempotency guard: only PENDING leave requests can transition on workflow completion
        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            return;
        }

        int year = leave.getStartDate().getYear();
        double paidDays = leave.getPaidDays() != null ? leave.getPaidDays() : leave.getDurationDays();
        String oldStatus = leave.getStatus();
        String empCode = leave.getEmployee() != null ? (leave.getEmployee().getEmployeeId() != null ? leave.getEmployee().getEmployeeId() : leave.getEmployee().getId().toString()) : "UNKNOWN";
        Long orgId = leave.getOrganization() != null ? leave.getOrganization().getId() : (leave.getEmployee() != null && leave.getEmployee().getOrganization() != null ? leave.getEmployee().getOrganization().getId() : null);

        if (event.getStatus() == ApprovalStatus.APPROVED) {
            leave.setStatus("APPROVED");
            leave.setApprovedAt(LocalDateTime.now());
            leave.setUpdatedAt(LocalDateTime.now());
            Leave saved = leaveRepository.save(leave);

            // Permanently commit/deduct balance from reserved pending balance (only paidDays)
            if (paidDays > 0) {
                balanceService.commitBalance(saved.getEmployee(), saved.getLeaveType(), year, paidDays);
            }

            // Record audit history
            historyRepository.save(new LeaveRequestHistory(
                    saved, "APPROVED", null, oldStatus, "APPROVED", "Approved via Approval Workflow Engine (" + event.getWorkflowInstanceId() + ")"
            ));

            String leaveTypeName = saved.getLeaveType() != null ? saved.getLeaveType().getName() : "LEAVE";
            eventPublisher.publishEvent(new LeaveApprovedEvent(
                    saved.getId(),
                    empCode,
                    orgId,
                    "WORKFLOW_ENGINE",
                    saved.getStartDate(),
                    saved.getEndDate(),
                    leaveTypeName
            ));
        } else if (event.getStatus() == ApprovalStatus.REJECTED) {
            leave.setStatus("REJECTED");
            leave.setRejectedAt(LocalDateTime.now());
            leave.setUpdatedAt(LocalDateTime.now());
            Leave saved = leaveRepository.save(leave);

            // Release reserved pending balance (only paidDays)
            if (paidDays > 0) {
                balanceService.releasePendingBalance(saved.getEmployee(), saved.getLeaveType(), year, paidDays);
            }

            // Record audit history
            historyRepository.save(new LeaveRequestHistory(
                    saved, "REJECTED", null, oldStatus, "REJECTED", "Rejected via Approval Workflow Engine (" + event.getWorkflowInstanceId() + ")"
            ));

            eventPublisher.publishEvent(new LeaveRejectedEvent(
                    saved.getId(),
                    empCode,
                    orgId,
                    "WORKFLOW_ENGINE",
                    "Rejected via Approval Workflow Engine (" + event.getWorkflowInstanceId() + ")"
            ));
        }
    }
}
