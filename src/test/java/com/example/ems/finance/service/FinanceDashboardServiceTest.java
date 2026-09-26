package com.example.ems.finance.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.impl.FinanceDashboardServiceImpl;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.schedule.dto.TodayScheduleResponse;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.security.context.TenantContext;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinanceDashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MyScheduleService myScheduleService;

    @InjectMocks
    private FinanceDashboardServiceImpl dashboardService;

    private Organization org;
    private Employee employee;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(10L);
        org.setName("Finance Org");
        TenantContext.setCurrentTenant(10L);

        employee = new Employee();
        employee.setId(101L);
        employee.setFullName("John Doe");
        employee.setEmail("john.doe@company.com");
        employee.setDepartment("Finance");
        employee.setDesignation("Senior Analyst");
        employee.setLocation("HQ Office, Chennai");
        employee.setOrganization(org);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("john.doe@company.com", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testFinanceDashboard_FullAggregationSuccess() {
        when(employeeRepository.findByEmailAndOrganizationId("john.doe@company.com", 10L))
                .thenReturn(Optional.of(employee));

        // 1. Attendance Mock
        Attendance att1 = new Attendance();
        att1.setStatus(AttendanceStatus.PRESENT);
        att1.setCheckInTime(Instant.now());
        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(eq(101L), any(), any(), eq(10L)))
                .thenReturn(List.of(att1));

        // 2. Leave Mock
        LeaveType cl = new LeaveType();
        cl.setName("CL");
        LeaveBalance lb = new LeaveBalance();
        lb.setLeaveType(cl);
        lb.setTotalEntitlement(12.0);
        lb.setUsedBalance(4.0);
        lb.setPendingBalance(0.0);
        when(leaveBalanceRepository.findByEmployeeIdAndYear(eq(101L), anyInt()))
                .thenReturn(List.of(lb));

        // 3. CTC Mock
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment();
        assignment.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        SalaryStructure structure = new SalaryStructure();
        structure.setCurrency("INR");
        assignment.setSalaryStructure(structure);
        EmployeeSalaryComponentValue val = new EmployeeSalaryComponentValue();
        val.setAmount(new BigDecimal("100000.00"));
        assignment.setComponentValues(List.of(val));
        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(10L), eq(101L), any()))
                .thenReturn(List.of(assignment));

        // 4. Rating Mock
        Appraisal appraisal = new Appraisal();
        appraisal.setId(55L);
        appraisal.setFinalRating(4.5);
        appraisal.setManagerReviewSubmittedAt(LocalDateTime.of(2025, 12, 1, 10, 0));
        when(appraisalRepository.findByOrganizationIdAndEmployeeId(10L, 101L))
                .thenReturn(List.of(appraisal));

        // 5. Expense Mock (1 draft)
        Expense draftExp = new Expense();
        draftExp.setStatus("DRAFT");
        when(expenseRepository.findByEmployeeId(101L))
                .thenReturn(List.of(draftExp));

        // 6. Schedule Mock
        TodayScheduleResponse schedResponse = new TodayScheduleResponse();
        TodayScheduleResponse.ShiftInfo shiftInfo = new TodayScheduleResponse.ShiftInfo();
        shiftInfo.setName("Morning Shift");
        shiftInfo.setStartTime("06:00");
        shiftInfo.setEndTime("14:00");
        shiftInfo.setLocation("HQ Office, Chennai");
        schedResponse.setShift(shiftInfo);
        when(myScheduleService.getTodaySchedule("john.doe@company.com"))
                .thenReturn(schedResponse);

        // 7. Finance Team Mock
        Employee colleague = new Employee();
        colleague.setId(102L);
        colleague.setFullName("Rajan Kumar");
        colleague.setDesignation("Finance Lead");
        colleague.setAvailability("ONLINE");
        when(employeeRepository.findByOrganizationIdAndDepartment(10L, "Finance"))
                .thenReturn(List.of(employee, colleague));

        // Act
        FinanceDashboardResponseDto result = dashboardService.getFinanceDashboard();

        // Assert
        assertThat(result).isNotNull();

        // Attendance
        assertThat(result.attendance()).isNotNull();
        assertThat(result.attendance().presentDays()).isEqualTo(1);
        assertThat(result.attendance().workingDays()).isGreaterThan(0);

        // Leave
        assertThat(result.leaveBalance()).isNotNull();
        assertThat(result.leaveBalance().totalRemaining()).isEqualTo(8);
        assertThat(result.leaveBalance().balances()).containsEntry("CL", 8);

        // CTC
        assertThat(result.ctc()).isNotNull();
        assertThat(result.ctc().annualCtc()).isEqualByComparingTo("1200000.00");
        assertThat(result.ctc().displayValue()).isEqualTo("₹12L");
        assertThat(result.ctc().currency()).isEqualTo("INR");

        // Rating
        assertThat(result.rating()).isNotNull();
        assertThat(result.rating().rating()).isEqualTo(4.5);
        assertThat(result.rating().lastReviewDate()).isEqualTo("2025-12-01");

        // Pending Actions
        assertThat(result.pendingActions()).isNotEmpty();
        assertThat(result.pendingActions())
                .extracting(FinancePendingActionDto::type)
                .contains("LEAVE", "INVESTMENT_DECLARATION", "EXPENSE");

        // Today Schedule
        assertThat(result.todaySchedule()).isNotNull();
        assertThat(result.todaySchedule().shiftName()).isEqualTo("Morning Shift");
        assertThat(result.todaySchedule().startTime()).isEqualTo("06:00");
        assertThat(result.todaySchedule().endTime()).isEqualTo("14:00");

        // Finance Team
        assertThat(result.financeTeam()).hasSize(2);
        assertThat(result.financeTeam().get(1).initials()).isEqualTo("RK");
        assertThat(result.financeTeam().get(1).name()).isEqualTo("Rajan Kumar");
    }

    @Test
    void testFinanceDashboard_MissingOptionalData_GracefulHandling() {
        when(employeeRepository.findByEmailAndOrganizationId("john.doe@company.com", 10L))
                .thenReturn(Optional.of(employee));

        // Return empty/null for optional modules
        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(leaveBalanceRepository.findByEmployeeIdAndYear(any(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(salaryAssignmentRepository.findActiveAssignmentsForDate(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(payrollRepository.findByEmployeeId(any()))
                .thenReturn(Collections.emptyList());
        when(appraisalRepository.findByOrganizationIdAndEmployeeId(any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseRepository.findByEmployeeId(any()))
                .thenReturn(Collections.emptyList());
        when(employeeRepository.findByOrganizationIdAndDepartment(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act - should execute safely without NullPointerException
        FinanceDashboardResponseDto result = dashboardService.getFinanceDashboard();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.attendance().presentDays()).isEqualTo(0);
        assertThat(result.leaveBalance().totalRemaining()).isEqualTo(0);
        assertThat(result.ctc().annualCtc()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.rating().rating()).isNull();
        assertThat(result.rating().lastReviewDate()).isNull();
        assertThat(result.financeTeam()).isEmpty();
    }

    @Test
    void testFinanceDashboard_Unauthenticated_ThrowsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> dashboardService.getFinanceDashboard())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Full authentication is required");
    }

    @Test
    void testFinanceDashboard_DoesNotMutateDomainState() {
        when(employeeRepository.findByEmailAndOrganizationId("john.doe@company.com", 10L))
                .thenReturn(Optional.of(employee));

        dashboardService.getFinanceDashboard();

        // Verify zero write/save interactions across all repositories
        verify(employeeRepository, never()).save(any());
        verify(attendanceRepository, never()).save(any());
        verify(leaveBalanceRepository, never()).save(any());
        verify(salaryAssignmentRepository, never()).save(any());
        verify(payrollRepository, never()).save(any());
        verify(appraisalRepository, never()).save(any());
        verify(expenseRepository, never()).save(any());
    }
}
