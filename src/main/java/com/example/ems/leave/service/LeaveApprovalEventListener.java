package com.example.ems.leave.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveRequestHistory;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveRequestHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

        int year = leave.getStartDate().getYear();

        if (event.getStatus() == ApprovalStatus.APPROVED) {
            String oldStatus = leave.getStatus();
            leave.setStatus("APPROVED");
            leave.setApprovedAt(LocalDateTime.now());
            leave.setUpdatedAt(LocalDateTime.now());
            leaveRepository.save(leave);

            // Permanently commit/deduct balance from reserved pending balance
            balanceService.commitBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());

            // Record audit history
            historyRepository.save(new LeaveRequestHistory(
                    leave, "APPROVED", null, oldStatus, "APPROVED", "Approved via Approval Workflow Engine (" + event.getWorkflowInstanceId() + ")"
            ));
        } else if (event.getStatus() == ApprovalStatus.REJECTED) {
            String oldStatus = leave.getStatus();
            leave.setStatus("REJECTED");
            leave.setRejectedAt(LocalDateTime.now());
            leave.setUpdatedAt(LocalDateTime.now());
            leaveRepository.save(leave);

            // Release reserved pending balance
            balanceService.releasePendingBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());

            // Record audit history
            historyRepository.save(new LeaveRequestHistory(
                    leave, "REJECTED", null, oldStatus, "REJECTED", "Rejected via Approval Workflow Engine (" + event.getWorkflowInstanceId() + ")"
            ));
        }
    }
}
