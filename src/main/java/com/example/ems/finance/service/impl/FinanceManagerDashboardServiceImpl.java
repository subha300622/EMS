package com.example.ems.finance.service.impl;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.finance.dto.FinanceExpenseListItem;
import com.example.ems.finance.dto.manager.*;
import com.example.ems.finance.service.FinanceExpenseService;
import com.example.ems.finance.service.FinanceManagerDashboardService;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.service.PermissionCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceManagerDashboardServiceImpl implements FinanceManagerDashboardService {

    private static final Logger log = LoggerFactory.getLogger(FinanceManagerDashboardServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PermissionCheckService permissionCheckService;

    @Autowired
    private FinanceExpenseService financeExpenseService;

    @Override
    @Transactional(readOnly = true)
    public FinanceManagerDashboardResponseDto getManagerFinanceDashboard() {
        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);

        List<Employee> team = getAuthorizedTeam(manager, orgId);
        List<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toList());

        ManagerTeamSummaryDto teamSummary = buildTeamSummary(team, teamIds, orgId);
        ManagerLeaveSummaryDto leaveSummary = buildLeaveSummary(teamIds);
        ManagerExpenseSummaryDto expenseSummary = buildExpenseSummary(teamIds);
        ManagerPayrollSummaryDto payrollSummary = buildPayrollSummary(team, orgId);
        List<ManagerPendingActionDto> pendingActions = buildPendingActions(
                leaveSummary.getPendingRequests(),
                expenseSummary.getPendingClaims());
        List<ManagerTeamMemberDto> teamMembers = buildTeamMembers(team, orgId);

        return new FinanceManagerDashboardResponseDto(
                teamSummary,
                leaveSummary,
                expenseSummary,
                payrollSummary,
                pendingActions,
                teamMembers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagerTeamMemberDto> getManagerTeam() {
        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);
        return buildTeamMembers(team, orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerTeamMemberDto getManagerTeamMember(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID must not be null");
        }
        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);

        Employee target = team.stream()
                .filter(e -> employeeId.equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException(
                        "Access Denied: Target employee #" + employeeId + " is not within your reporting hierarchy"));

        List<ManagerTeamMemberDto> dtos = buildTeamMembers(List.of(target), orgId);
        return dtos.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManagerPendingActionDto> getPendingActions() {
        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);
        List<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toList());

        ManagerLeaveSummaryDto leaveSummary = buildLeaveSummary(teamIds);
        ManagerExpenseSummaryDto expenseSummary = buildExpenseSummary(teamIds);

        return buildPendingActions(leaveSummary.getPendingRequests(), expenseSummary.getPendingClaims());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceExpenseListItem> getPendingExpenses() {
        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);
        List<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toList());

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<FinanceExpenseListItem> pendingItems = new ArrayList<>();
        for (Long empId : teamIds) {
            List<Expense> expenses = expenseRepository.findByEmployeeId(empId);
            for (Expense e : expenses) {
                if (isPendingExpense(e.getExpenseStatus())) {
                    Employee emp = e.getEmployee();
                    LocalDate submittedDate = e.getSubmittedAt() != null
                            ? e.getSubmittedAt().toLocalDate()
                            : e.getExpenseDate();
                    boolean receiptAttached = (e.getAttachmentUrl() != null && !e.getAttachmentUrl().isBlank())
                            || (e.getReceipts() != null && !e.getReceipts().isEmpty());

                    pendingItems.add(new FinanceExpenseListItem(
                            e.getId(),
                            emp != null ? emp.getId() : null,
                            emp != null ? emp.getFullName() : null,
                            emp != null ? emp.getDepartment() : null,
                            e.getCategory() != null ? e.getCategory().getCode() : null,
                            e.getDescription(),
                            e.getAmount(),
                            receiptAttached,
                            submittedDate,
                            e.getExpenseStatus() != null ? e.getExpenseStatus().name() : "PENDING"));
                }
            }
        }

        return pendingItems;
    }

    @Override
    @Transactional
    public void approveExpense(Long expenseId, String remarks) {
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_EXPENSE_APPROVE);

        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);
        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        if (expense.getEmployee() == null || !teamIds.contains(expense.getEmployee().getId())) {
            throw new AccessDeniedException(
                    "Access Denied: Expense #" + expenseId + " belongs to an employee outside your reporting hierarchy");
        }

        String callerEmail = getCallerEmail();
        financeExpenseService.approveExpense(expenseId, remarks, callerEmail);
    }

    @Override
    @Transactional
    public void rejectExpense(Long expenseId, String reason) {
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_EXPENSE_APPROVE);

        Employee manager = resolveCurrentEmployee();
        Long orgId = resolveOrganizationId(manager);
        List<Employee> team = getAuthorizedTeam(manager, orgId);
        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        if (expense.getEmployee() == null || !teamIds.contains(expense.getEmployee().getId())) {
            throw new AccessDeniedException(
                    "Access Denied: Expense #" + expenseId + " belongs to an employee outside your reporting hierarchy");
        }

        String callerEmail = getCallerEmail();
        financeExpenseService.rejectExpense(expenseId, reason, callerEmail);
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────────

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Full authentication is required to access the manager finance dashboard.");
        }

        final String email = getCallerEmail();
        Long orgId = TenantContext.getOrganizationId();
        Optional<Employee> empOpt;
        if (orgId != null) {
            empOpt = employeeRepository.findByEmailAndOrganizationId(email, orgId)
                    .or(() -> employeeRepository.findByEmail(email));
        } else {
            empOpt = employeeRepository.findByEmail(email);
        }

        return empOpt.orElseThrow(
                () -> new ResourceNotFoundException("Employee profile not found for user: " + email));
    }

    private String getCallerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal p && p.getEmail() != null) {
            return p.getEmail();
        }
        return auth.getName();
    }

    private Long resolveOrganizationId(Employee employee) {
        Long orgId = TenantContext.getOrganizationId();
        if (orgId == null && employee.getOrganization() != null) {
            orgId = employee.getOrganization().getId();
        }
        return orgId;
    }

    private List<Employee> getAuthorizedTeam(Employee manager, Long orgId) {
        if (manager == null) {
            return Collections.emptyList();
        }
        if (orgId != null) {
            return employeeRepository.findByOrganizationIdAndManagerId(orgId, manager.getId());
        }
        return employeeRepository.findByManagerId(manager.getId());
    }

    private ManagerTeamSummaryDto buildTeamSummary(List<Employee> team, List<Long> teamIds, Long orgId) {
        int totalEmployees = team.size();
        if (totalEmployees == 0) {
            return new ManagerTeamSummaryDto(0, 0, 0, 0.0);
        }

        LocalDate today = LocalDate.now();
        List<Attendance> attendances;
        if (orgId != null) {
            attendances = attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(teamIds, today, orgId);
        } else {
            attendances = attendanceRepository.findByDate(today).stream()
                    .filter(a -> a.getEmployee() != null && teamIds.contains(a.getEmployee().getId()))
                    .collect(Collectors.toList());
        }

        Set<Long> presentEmpIds = new HashSet<>();
        Set<Long> onLeaveEmpIds = new HashSet<>();

        for (Attendance a : attendances) {
            if (a.getEmployee() == null) continue;
            AttendanceStatus st = a.getAttendanceStatus();
            if (st == AttendanceStatus.PRESENT || st == AttendanceStatus.LATE || st == AttendanceStatus.HALF_DAY
                    || st == AttendanceStatus.WORKING || st == AttendanceStatus.COMPLETED) {
                presentEmpIds.add(a.getEmployee().getId());
            } else if (st == AttendanceStatus.LEAVE) {
                onLeaveEmpIds.add(a.getEmployee().getId());
            }
        }

        // Cross-check active approved leaves today
        try {
            List<Leave> todayLeaves = leaveRepository.findByEmployeeIdInAndStatus(teamIds, "APPROVED");
            for (Leave l : todayLeaves) {
                if (l.getEmployee() == null) continue;
                if (l.getStartDate() != null && l.getEndDate() != null
                        && !today.isBefore(l.getStartDate()) && !today.isAfter(l.getEndDate())) {
                    onLeaveEmpIds.add(l.getEmployee().getId());
                }
            }
        } catch (Exception ex) {
            log.warn("Error cross-checking today leaves: {}", ex.getMessage());
        }

        int presentToday = presentEmpIds.size();
        int onLeaveToday = onLeaveEmpIds.size();
        double attendancePercentage = BigDecimal.valueOf((presentToday * 100.0) / totalEmployees)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();

        return new ManagerTeamSummaryDto(totalEmployees, presentToday, onLeaveToday, attendancePercentage);
    }

    private ManagerLeaveSummaryDto buildLeaveSummary(List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return new ManagerLeaveSummaryDto(0L, 0L, 0.0);
        }

        long pendingRequests = 0;
        try {
            pendingRequests = leaveRepository.findByEmployeeIdInAndStatus(teamIds, "PENDING").size();
        } catch (Exception ex) {
            log.warn("Error querying pending leaves: {}", ex.getMessage());
        }

        long approvedThisMonth = 0;
        double teamLeaveDays = 0.0;

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        try {
            List<Leave> approvedLeaves = leaveRepository.findByEmployeeIdInAndStatus(teamIds, "APPROVED");
            for (Leave l : approvedLeaves) {
                if (l.getStartDate() != null && !l.getStartDate().isBefore(startOfMonth)
                        && !l.getStartDate().isAfter(endOfMonth)) {
                    approvedThisMonth++;
                    if (l.getDurationDays() != null) {
                        teamLeaveDays += l.getDurationDays();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Error querying approved leaves: {}", ex.getMessage());
        }

        teamLeaveDays = BigDecimal.valueOf(teamLeaveDays).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new ManagerLeaveSummaryDto(pendingRequests, approvedThisMonth, teamLeaveDays);
    }

    private ManagerExpenseSummaryDto buildExpenseSummary(List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return new ManagerExpenseSummaryDto(0L, BigDecimal.ZERO, 0L, BigDecimal.ZERO);
        }

        long pendingClaims = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long approvedThisMonth = 0;
        BigDecimal approvedAmount = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        int curMonth = today.getMonthValue();
        int curYear = today.getYear();

        for (Long empId : teamIds) {
            List<Expense> expenses = expenseRepository.findByEmployeeId(empId);
            for (Expense e : expenses) {
                if (isPendingExpense(e.getExpenseStatus())) {
                    pendingClaims++;
                    if (e.getAmount() != null) {
                        pendingAmount = pendingAmount.add(e.getAmount());
                    }
                } else if (isApprovedExpense(e.getExpenseStatus())) {
                    LocalDate expDate = e.getExpenseDate() != null
                            ? e.getExpenseDate()
                            : (e.getSubmittedAt() != null ? e.getSubmittedAt().toLocalDate() : null);
                    if (expDate != null && expDate.getMonthValue() == curMonth && expDate.getYear() == curYear) {
                        approvedThisMonth++;
                        if (e.getAmount() != null) {
                            approvedAmount = approvedAmount.add(e.getAmount());
                        }
                    }
                }
            }
        }

        return new ManagerExpenseSummaryDto(pendingClaims, pendingAmount, approvedThisMonth, approvedAmount);
    }

    private ManagerPayrollSummaryDto buildPayrollSummary(List<Employee> team, Long orgId) {
        // Enforce explicit permission check: finance.payroll.view or finance.salary.view
        boolean canViewPayroll = permissionCheckService.hasAnyPermission(
                PermissionRegistry.FINANCE_PAYROLL_VIEW,
                PermissionRegistry.FINANCE_SALARY_VIEW);

        if (!canViewPayroll) {
            return null;
        }

        int employeeCount = team.size();
        BigDecimal totalMonthlyGross = BigDecimal.ZERO;
        String lastPayrollStatus = "PROCESSED";

        LocalDate today = LocalDate.now();
        for (Employee emp : team) {
            BigDecimal empGross = BigDecimal.ZERO;

            // 1. Try active salary assignments
            List<EmployeeSalaryAssignment> assignments = Collections.emptyList();
            if (orgId != null) {
                assignments = salaryAssignmentRepository.findActiveAssignmentsForDate(orgId, emp.getId(), today);
                if (assignments.isEmpty()) {
                    assignments = salaryAssignmentRepository
                            .findByOrganizationIdAndEmployeeIdOrderByEffectiveFromDesc(orgId, emp.getId());
                }
            }
            if (!assignments.isEmpty() && assignments.get(0).getComponentValues() != null) {
                for (EmployeeSalaryComponentValue cv : assignments.get(0).getComponentValues()) {
                    if (cv.getAmount() != null) {
                        empGross = empGross.add(cv.getAmount());
                    }
                }
            } else {
                // 2. Fallback to latest payroll records
                List<Payroll> payrollList = payrollRepository.findByEmployeeId(emp.getId());
                if (!payrollList.isEmpty()) {
                    Payroll latest = payrollList.stream()
                            .max(Comparator.comparing(Payroll::getYear, Comparator.nullsFirst(Comparator.naturalOrder()))
                                    .thenComparing(Payroll::getMonth, Comparator.nullsFirst(Comparator.naturalOrder())))
                            .orElse(payrollList.get(0));
                    empGross = (latest.getBasicSalary() != null ? latest.getBasicSalary() : BigDecimal.ZERO)
                            .add(latest.getAllowances() != null ? latest.getAllowances() : BigDecimal.ZERO);
                    if (latest.getStatus() != null && !latest.getStatus().isBlank()) {
                        lastPayrollStatus = latest.getStatus();
                    }
                }
            }
            totalMonthlyGross = totalMonthlyGross.add(empGross);
        }

        return new ManagerPayrollSummaryDto(employeeCount, totalMonthlyGross, lastPayrollStatus);
    }

    private List<ManagerPendingActionDto> buildPendingActions(long pendingLeaves, long pendingExpenses) {
        List<ManagerPendingActionDto> actions = new ArrayList<>();
        actions.add(new ManagerPendingActionDto("LEAVE_APPROVAL", pendingLeaves, "Leave Requests Awaiting Approval"));
        actions.add(new ManagerPendingActionDto("EXPENSE_APPROVAL", pendingExpenses, "Expense Claims Awaiting Approval"));
        return actions;
    }

    private List<ManagerTeamMemberDto> buildTeamMembers(List<Employee> team, Long orgId) {
        if (team.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        int workingDays = calculateWorkingDays(startOfMonth, today);

        List<ManagerTeamMemberDto> memberDtos = new ArrayList<>();
        for (Employee emp : team) {
            // Attendance percentage for the month
            List<Attendance> attendances;
            if (orgId != null) {
                attendances = attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                        emp.getId(), startOfMonth, today, orgId);
            } else {
                attendances = attendanceRepository.findByEmployeeId(emp.getId()).stream()
                        .filter(a -> a.getDate() != null && !a.getDate().isBefore(startOfMonth) && !a.getDate().isAfter(today))
                        .collect(Collectors.toList());
            }

            long presentDays = attendances.stream()
                    .filter(a -> a.getAttendanceStatus() == AttendanceStatus.PRESENT
                            || a.getAttendanceStatus() == AttendanceStatus.LATE
                            || a.getAttendanceStatus() == AttendanceStatus.WORKING
                            || a.getAttendanceStatus() == AttendanceStatus.COMPLETED)
                    .count();

            double attendancePct = workingDays > 0
                    ? BigDecimal.valueOf((presentDays * 100.0) / workingDays).setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 100.0;

            // Available leave balance
            double leaveBalance = 0.0;
            try {
                List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(emp.getId());
                for (LeaveBalance b : balances) {
                    if (b.getAvailableBalance() != null) {
                        leaveBalance += b.getAvailableBalance();
                    }
                }
            } catch (Exception ex) {
                log.warn("Error fetching leave balance for employee #{}: {}", emp.getId(), ex.getMessage());
            }

            // Pending expenses
            long pendingExpenses = 0;
            try {
                List<Expense> expenses = expenseRepository.findByEmployeeId(emp.getId());
                pendingExpenses = expenses.stream()
                        .filter(e -> isPendingExpense(e.getExpenseStatus()))
                        .count();
            } catch (Exception ex) {
                log.warn("Error fetching expenses for employee #{}: {}", emp.getId(), ex.getMessage());
            }

            memberDtos.add(new ManagerTeamMemberDto(
                    emp.getId(),
                    emp.getFullName(),
                    emp.getDesignation() != null ? emp.getDesignation() : "",
                    attendancePct,
                    leaveBalance,
                    pendingExpenses));
        }

        return memberDtos;
    }

    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            DayOfWeek dow = cur.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            cur = cur.plusDays(1);
        }
        return count == 0 ? 1 : count;
    }

    private boolean isPendingExpense(ExpenseStatus status) {
        if (status == null) {
            return false;
        }
        return status == ExpenseStatus.PENDING
                || status == ExpenseStatus.SUBMITTED
                || status == ExpenseStatus.PENDING_MANAGER_APPROVAL
                || status == ExpenseStatus.PENDING_FINANCE_APPROVAL
                || status == ExpenseStatus.PENDING_APPROVAL;
    }

    private boolean isApprovedExpense(ExpenseStatus status) {
        if (status == null) {
            return false;
        }
        return status == ExpenseStatus.APPROVED
                || status == ExpenseStatus.APPROVED_FOR_PAYMENT
                || status == ExpenseStatus.REIMBURSED
                || status == ExpenseStatus.PAID;
    }
}
