package com.example.ems.common.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.schedule.entity.MyScheduleChangeRequest;
import com.example.ems.schedule.repository.MyScheduleChangeRequestRepository;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.asset.entity.MyAssetRequest;
import com.example.ems.asset.repository.MyAssetRequestRepository;
import com.example.ems.common.dto.manager.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalWidgetService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private MyScheduleChangeRequestRepository myScheduleChangeRequestRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MyAssetRequestRepository myAssetRequestRepository;

    @Autowired
    private Environment environment;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    public PendingApprovalCountsDto getApprovalSummary(Employee manager, List<Employee> team) {
        if (team.isEmpty()) {
            if (isDevProfile()) {
                return new PendingApprovalCountsDto(5L, 2L, 1L, 3L, 0L, 11L);
            } else {
                return new PendingApprovalCountsDto(0L, 0L, 0L, 0L, 0L, 0L);
            }
        }

        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());

        long leaveCount = leaveRepository.findByStatus("PENDING").stream()
                .filter(l -> l.getEmployee() != null && teamIds.contains(l.getEmployee().getId()))
                .count();

        long expenseCount = expenseRepository.findAll().stream()
                .filter(e -> e.getExpenseStatus() == ExpenseStatus.PENDING && e.getEmployee() != null && teamIds.contains(e.getEmployee().getId()))
                .count();

        long shiftCount = myScheduleChangeRequestRepository.findAll().stream()
                .filter(s -> "PENDING_MANAGER_APPROVAL".equalsIgnoreCase(s.getStatus()) && s.getEmployee() != null && teamIds.contains(s.getEmployee().getId()))
                .count();

        long goalCount = goalRepository.findByStatus("PENDING").stream()
                .filter(g -> g.getEmployee() != null && teamIds.contains(g.getEmployee().getId()))
                .count();

        long assetCount = myAssetRequestRepository.findAll().stream()
                .filter(a -> "PENDING_MANAGER_APPROVAL".equalsIgnoreCase(a.getStatus()) && a.getEmployee() != null && teamIds.contains(a.getEmployee().getId()))
                .count();

        long total = leaveCount + expenseCount + shiftCount + goalCount + assetCount;

        if (total == 0 && isDevProfile()) {
            return new PendingApprovalCountsDto(5L, 2L, 1L, 3L, 0L, 11L);
        }

        return new PendingApprovalCountsDto(leaveCount, expenseCount, shiftCount, goalCount, assetCount, total);
    }

    public List<PendingApprovalDto> getPendingApprovals(Employee manager, List<Employee> team) {
        List<PendingApprovalDto> result = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                result.add(new PendingApprovalDto(101L, 2L, "Priya Sharma", "LEAVE", "2026-05-20", ApprovalStatus.PENDING));
            }
            return result;
        }

        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());

        // 1. Leaves
        List<Leave> pendingLeaves = leaveRepository.findByStatus("PENDING");
        long approvalId = 101;
        for (Leave l : pendingLeaves) {
            if (l.getEmployee() != null && teamIds.contains(l.getEmployee().getId())) {
                result.add(new PendingApprovalDto(
                        approvalId++,
                        l.getEmployee().getId(),
                        l.getEmployee().getFullName(),
                        "LEAVE",
                        l.getAppliedAt() != null ? l.getAppliedAt().toLocalDate().toString() : "2026-05-20",
                        ApprovalStatus.PENDING
                ));
            }
        }

        // 2. Shift Changes
        List<MyScheduleChangeRequest> pendingShifts = myScheduleChangeRequestRepository.findAll().stream()
                .filter(s -> "PENDING_MANAGER_APPROVAL".equalsIgnoreCase(s.getStatus()) && s.getEmployee() != null && teamIds.contains(s.getEmployee().getId()))
                .collect(Collectors.toList());
        for (MyScheduleChangeRequest s : pendingShifts) {
            result.add(new PendingApprovalDto(
                    approvalId++,
                    s.getEmployee().getId(),
                    s.getEmployee().getFullName(),
                    "SHIFT_CHANGE",
                    s.getSubmittedAt() != null ? s.getSubmittedAt().toLocalDate().toString() : "2026-05-20",
                    ApprovalStatus.PENDING_MANAGER_APPROVAL
            ));
        }

        // 3. Goal Approvals
        List<Goal> pendingGoals = goalRepository.findByStatus("PENDING").stream()
                .filter(g -> g.getEmployee() != null && teamIds.contains(g.getEmployee().getId()))
                .collect(Collectors.toList());
        for (Goal g : pendingGoals) {
            result.add(new PendingApprovalDto(
                    approvalId++,
                    g.getEmployee().getId(),
                    g.getEmployee().getFullName(),
                    "GOAL",
                    "2026-05-20",
                    ApprovalStatus.PENDING
            ));
        }

        // 4. Expense Approvals
        List<Expense> pendingExpenses = expenseRepository.findAll().stream()
                .filter(e -> e.getExpenseStatus() == ExpenseStatus.PENDING && e.getEmployee() != null && teamIds.contains(e.getEmployee().getId()))
                .collect(Collectors.toList());
        for (Expense e : pendingExpenses) {
            result.add(new PendingApprovalDto(
                    approvalId++,
                    e.getEmployee().getId(),
                    e.getEmployee().getFullName(),
                    "EXPENSE",
                    e.getSubmittedAt() != null ? e.getSubmittedAt().toLocalDate().toString() : "2026-05-20",
                    ApprovalStatus.PENDING
            ));
        }

        // 5. Asset Requests
        List<MyAssetRequest> pendingAssets = myAssetRequestRepository.findAll().stream()
                .filter(a -> "PENDING_MANAGER_APPROVAL".equalsIgnoreCase(a.getStatus()) && a.getEmployee() != null && teamIds.contains(a.getEmployee().getId()))
                .collect(Collectors.toList());
        for (MyAssetRequest a : pendingAssets) {
            result.add(new PendingApprovalDto(
                    approvalId++,
                    a.getEmployee().getId(),
                    a.getEmployee().getFullName(),
                    "ASSET",
                    a.getRequestedAt() != null ? a.getRequestedAt().toLocalDate().toString() : "2026-05-20",
                    ApprovalStatus.PENDING_MANAGER_APPROVAL
            ));
        }

        if (result.isEmpty() && isDevProfile()) {
            result.add(new PendingApprovalDto(101L, team.get(0).getId(), team.get(0).getFullName(), "LEAVE", "2026-05-20", ApprovalStatus.PENDING));
        }
        return result;
    }

    public List<LeaveSummaryDto> getLeaveSummary(Employee manager, List<Employee> team) {
        List<LeaveSummaryDto> result = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                result.add(new LeaveSummaryDto(1L, "Priya Sharma", "Casual Leave", "2026-06-25", "2026-06-27", ApprovalStatus.PENDING));
            }
            return result;
        }

        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());
        List<Leave> leaves = leaveRepository.findAll().stream()
                .filter(l -> l.getEmployee() != null && teamIds.contains(l.getEmployee().getId()))
                .collect(Collectors.toList());

        for (Leave l : leaves) {
            ApprovalStatus status = ApprovalStatus.PENDING;
            if ("APPROVED".equalsIgnoreCase(l.getStatus())) status = ApprovalStatus.APPROVED;
            else if ("REJECTED".equalsIgnoreCase(l.getStatus())) status = ApprovalStatus.REJECTED;

            result.add(new LeaveSummaryDto(
                    l.getEmployee().getId(),
                    l.getEmployee().getFullName(),
                    l.getLeaveType() != null ? l.getLeaveType().getName() : "Leave",
                    l.getStartDate() != null ? l.getStartDate().toString() : "2026-06-25",
                    l.getEndDate() != null ? l.getEndDate().toString() : "2026-06-27",
                    status
            ));
        }

        if (result.isEmpty() && isDevProfile()) {
            result.add(new LeaveSummaryDto(team.get(0).getId(), team.get(0).getFullName(), "Casual Leave", "2026-06-25", "2026-06-27", ApprovalStatus.PENDING));
        }
        return result;
    }
}
