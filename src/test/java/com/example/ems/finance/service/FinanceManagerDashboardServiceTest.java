package com.example.ems.finance.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.finance.dto.manager.*;
import com.example.ems.finance.service.impl.FinanceManagerDashboardServiceImpl;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinanceManagerDashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private FinanceExpenseService financeExpenseService;

    @InjectMocks
    private FinanceManagerDashboardServiceImpl managerDashboardService;

    private Organization org;
    private Employee manager;
    private Employee reportee;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("manager@company.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        org = new Organization();
        org.setId(1L);
        org.setName("Acme Corp");
        TenantContext.setCurrentTenant(1L);

        manager = new Employee();
        manager.setId(10L);
        manager.setFullName("Manager Alpha");
        manager.setEmail("manager@company.com");
        manager.setOrganization(org);

        reportee = new Employee();
        reportee.setId(101L);
        reportee.setFullName("Rajan Kumar");
        reportee.setEmail("rajan.kumar@company.com");
        reportee.setDesignation("Senior Accountant");
        reportee.setOrganization(org);
        reportee.setManager(manager);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testGetManagerFinanceDashboard_WithPayrollPermission_ReturnsAggregatedData() {
        when(employeeRepository.findByEmailAndOrganizationId(eq("manager@company.com"), eq(1L)))
                .thenReturn(Optional.of(manager));
        when(employeeRepository.findByOrganizationIdAndManagerId(eq(1L), eq(10L)))
                .thenReturn(List.of(reportee));

        when(permissionCheckService.hasAnyPermission(any(), any())).thenReturn(true);

        // Attendance
        Attendance att = new Attendance();
        att.setEmployee(reportee);
        att.setDate(LocalDate.now());
        att.setStatus("PRESENT");
        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(anyList(), any(), eq(1L)))
                .thenReturn(List.of(att));

        // Leaves
        Leave leave = new Leave();
        leave.setEmployee(reportee);
        leave.setDurationDays(2.0);
        leave.setStartDate(LocalDate.now());
        leave.setEndDate(LocalDate.now());
        leave.setStatus("APPROVED");
        when(leaveRepository.findByEmployeeIdInAndStatus(anyList(), eq("APPROVED")))
                .thenReturn(List.of(leave));
        when(leaveRepository.findByEmployeeIdInAndStatus(anyList(), eq("PENDING")))
                .thenReturn(Collections.emptyList());

        // Expense
        Expense expense = new Expense();
        expense.setId(50L);
        expense.setEmployee(reportee);
        expense.setAmount(new BigDecimal("15000"));
        expense.setExpenseStatus(ExpenseStatus.PENDING);
        when(expenseRepository.findByEmployeeId(eq(101L)))
                .thenReturn(List.of(expense));

        // Salary
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment();
        assignment.setEmployee(reportee);
        EmployeeSalaryComponentValue cv = new EmployeeSalaryComponentValue();
        cv.setAmount(new BigDecimal("100000"));
        assignment.setComponentValues(List.of(cv));
        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(1L), eq(101L), any()))
                .thenReturn(List.of(assignment));

        FinanceManagerDashboardResponseDto response = managerDashboardService.getManagerFinanceDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getTeamSummary()).isNotNull();
        assertThat(response.getTeamSummary().getTotalEmployees()).isEqualTo(1);
        assertThat(response.getTeamSummary().getPresentToday()).isEqualTo(1);
        assertThat(response.getExpenseSummary()).isNotNull();
        assertThat(response.getExpenseSummary().getPendingClaims()).isEqualTo(1);
        assertThat(response.getExpenseSummary().getPendingAmount()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(response.getPayrollSummary()).isNotNull();
        assertThat(response.getPayrollSummary().getEmployees()).isEqualTo(1);
        assertThat(response.getPayrollSummary().getTotalMonthlyGross()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.getTeamMembers()).hasSize(1);
        assertThat(response.getTeamMembers().get(0).getName()).isEqualTo("Rajan Kumar");
    }

    @Test
    void testGetManagerFinanceDashboard_WithoutPayrollPermission_OmitPayrollSummary() {
        when(employeeRepository.findByEmailAndOrganizationId(eq("manager@company.com"), eq(1L)))
                .thenReturn(Optional.of(manager));
        when(employeeRepository.findByOrganizationIdAndManagerId(eq(1L), eq(10L)))
                .thenReturn(List.of(reportee));

        when(permissionCheckService.hasAnyPermission(any(), any())).thenReturn(false);

        FinanceManagerDashboardResponseDto response = managerDashboardService.getManagerFinanceDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getTeamSummary()).isNotNull();
        assertThat(response.getPayrollSummary()).isNull();
    }

    @Test
    void testGetManagerTeamMember_OutsideHierarchy_ThrowsAccessDenied() {
        when(employeeRepository.findByEmailAndOrganizationId(eq("manager@company.com"), eq(1L)))
                .thenReturn(Optional.of(manager));
        when(employeeRepository.findByOrganizationIdAndManagerId(eq(1L), eq(10L)))
                .thenReturn(List.of(reportee));

        assertThatThrownBy(() -> managerDashboardService.getManagerTeamMember(999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not within your reporting hierarchy");
    }

    @Test
    void testApproveExpense_OutsideHierarchy_ThrowsAccessDenied() {
        when(employeeRepository.findByEmailAndOrganizationId(eq("manager@company.com"), eq(1L)))
                .thenReturn(Optional.of(manager));
        when(employeeRepository.findByOrganizationIdAndManagerId(eq(1L), eq(10L)))
                .thenReturn(List.of(reportee));

        Employee outsider = new Employee();
        outsider.setId(999L);
        Expense outsideExpense = new Expense();
        outsideExpense.setId(88L);
        outsideExpense.setEmployee(outsider);

        when(expenseRepository.findById(eq(88L))).thenReturn(Optional.of(outsideExpense));

        assertThatThrownBy(() -> managerDashboardService.approveExpense(88L, "Approved"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("outside your reporting hierarchy");
    }
}
