package com.example.ems.dashboard;

import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceCalendarService;
import com.example.ems.employee.dto.dashboard.EmployeeAttendanceDetailSummaryDto;
import com.example.ems.employee.dto.dashboard.EmployeeDashboardResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.employee.service.actioncenter.EmployeeActionCenterService;
import com.example.ems.employee.service.impl.EmployeeDashboardServiceImpl;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeSelfScopeTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceCalendarService attendanceCalendarService;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private PerformanceReviewRecordRepository reviewRecordRepository;

    @Mock
    private EmployeeActionCenterService actionCenterService;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private EmployeeDashboardServiceImpl dashboardService;

    private Employee employeeA;
    private Employee employeeB;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        employeeA = new Employee();
        employeeA.setId(101L);
        employeeA.setEmail("empA@company.com");
        employeeA.setFullName("Employee Alpha");
        employeeA.setOrganization(new com.example.ems.organization.entity.Organization());
        employeeA.getOrganization().setId(1L);

        employeeB = new Employee();
        employeeB.setId(202L);
        employeeB.setEmail("empB@company.com");
        employeeB.setFullName("Employee Beta");
        employeeB.setOrganization(new com.example.ems.organization.entity.Organization());
        employeeB.getOrganization().setId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testEmployeeDashboard_StrictlySelfScoped_NeverReturnsAnotherEmployeeData() {
        // Authenticate as Employee A
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empA@company.com", "password", Collections.emptyList())
        );
        when(employeeRepository.findByEmail("empA@company.com")).thenReturn(Optional.of(employeeA));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(eq(101L), any(), any(), eq(1L)))
                .thenReturn(Collections.emptyList());
        when(leaveBalanceRepository.findByEmployeeId(101L))
                .thenReturn(Collections.emptyList());
        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(1L), eq(101L), any()))
                .thenReturn(Collections.emptyList());
        when(payrollRepository.findByEmployeeId(101L))
                .thenReturn(Collections.emptyList());
        when(reviewRecordRepository.findByOrganizationIdAndEmployeeId(1L, 101L))
                .thenReturn(Collections.emptyList());
        when(actionCenterService.getActionCenterResponse(employeeA, 1L))
                .thenReturn(new com.example.ems.employee.dto.dashboard.EmployeeActionCenterResponseDto(0, Collections.emptyList()));

        EmployeeDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        // Server strictly queries 101L (Employee A), regardless of any external query parameters
    }

    @Test
    void testGetAttendanceSummary_EmployeeACannotSeeEmployeeBAttendance() {
        // Authenticate as Employee A
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empA@company.com", "password", Collections.emptyList())
        );
        when(employeeRepository.findByEmail("empA@company.com")).thenReturn(Optional.of(employeeA));

        MonthlyAttendanceCalendarResponse cal = new MonthlyAttendanceCalendarResponse();
        cal.setWorkingDays(24);
        cal.setPresentDays(22);
        when(attendanceCalendarService.getMonthlyCalendar(anyInt(), anyInt())).thenReturn(cal);

        EmployeeAttendanceDetailSummaryDto summary = dashboardService.getAttendanceSummary();

        assertNotNull(summary);
        assertEquals(22, summary.presentDays());
        assertTrue(summary.attendancePercentage() > 90.0);
    }

    @Test
    void testGetMyLeaveById_IDORBlocked_AttemptAccessAnotherEmployeeLeaveThrowsException() {
        // Authenticate as Employee A
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empA@company.com", "password", Collections.emptyList())
        );
        when(employeeRepository.findByEmail("empA@company.com")).thenReturn(Optional.of(employeeA));

        // Leave belonging to Employee B (ID 202L)
        Leave leaveB = new Leave();
        leaveB.setId(777L);
        leaveB.setEmployee(employeeB);
        leaveB.setOrganization(employeeB.getOrganization());

        when(leaveService.getLeaveById(777L)).thenReturn(Optional.of(leaveB));

        // Employee A attempting to read Employee B's leave -> AccessDeniedException
        assertThrows(AccessDeniedException.class, () ->
                dashboardService.getMyLeaveById(777L));
    }
}
