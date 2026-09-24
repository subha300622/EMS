package com.example.ems.employee.service;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceCalendarService;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.dto.MyDocumentDetailsResponse;
import com.example.ems.employee.dto.dashboard.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.MyEmployeeDocumentRepository;
import com.example.ems.employee.service.actioncenter.EmployeeActionCenterService;
import com.example.ems.employee.service.impl.EmployeeDashboardServiceImpl;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.performance.entity.PerformanceReviewRecord;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import com.example.ems.performance.service.PerformanceReviewService;
import com.example.ems.security.context.TenantContext;
import com.example.ems.training.repository.TrainingProgressRepository;
import com.example.ems.training.service.TrainingAssignmentService;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeDashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private AttendanceCalendarService attendanceCalendarService;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveService leaveService;

    @Mock
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private PerformanceReviewRecordRepository reviewRecordRepository;

    @Mock
    private PerformanceReviewService reviewService;

    @Mock
    private MyEmployeeDocumentRepository employeeDocumentRepository;

    @Mock
    private MyDocumentService documentService;

    @Mock
    private TrainingAssignmentService trainingAssignmentService;

    @Mock
    private TrainingProgressRepository progressRepository;

    @Mock
    private EmployeeActionCenterService actionCenterService;

    @InjectMocks
    private EmployeeDashboardServiceImpl dashboardService;

    private Organization org;
    private Employee employee;
    private User user;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(10L);
        org.setName("Acme Corp");

        employee = new Employee();
        employee.setId(101L);
        employee.setEmail("sarah.jenkins@acme.com");
        employee.setFirstName("Sarah");
        employee.setLastName("Jenkins");
        employee.setOrganization(org);
        employee.setAnnualSalary(BigDecimal.valueOf(1800000));

        user = new User();
        user.setId(501L);
        user.setWorkEmail("sarah.jenkins@acme.com");
        user.setOrganization(org);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("sarah.jenkins@acme.com", "credentials", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        TenantContext.setCurrentTenant(10L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testGetDashboard_Success() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        // Mock Attendance
        MonthlyAttendanceCalendarResponse currentCal = new MonthlyAttendanceCalendarResponse();
        currentCal.setWorkingDays(24);
        currentCal.setPresentDays(22);
        MonthlyAttendanceCalendarResponse priorCal = new MonthlyAttendanceCalendarResponse();
        priorCal.setWorkingDays(24);
        priorCal.setPresentDays(21);
        when(attendanceCalendarService.getMonthlyCalendar(anyInt(), anyInt()))
                .thenReturn(currentCal, priorCal);

        // Mock Leave Balances
        LeaveType cl = new LeaveType();
        cl.setName("Casual Leave");
        LeaveBalance b1 = new LeaveBalance();
        b1.setLeaveType(cl);
        b1.setTotalEntitlement(10.0);
        b1.setUsedBalance(4.0);
        b1.setPendingBalance(0.0); // available = 6.0

        LeaveType el = new LeaveType();
        el.setName("Earned Leave");
        LeaveBalance b2 = new LeaveBalance();
        b2.setLeaveType(el);
        b2.setTotalEntitlement(12.0);
        b2.setUsedBalance(8.0);
        b2.setPendingBalance(0.0); // available = 4.0

        when(leaveBalanceRepository.findByEmployeeId(101L)).thenReturn(List.of(b1, b2));

        // Mock Compensation (Salary Assignment)
        SalaryStructure struct = new SalaryStructure();
        struct.setCurrency("INR");
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment();
        assignment.setSalaryStructure(struct);
        assignment.setEffectiveFrom(LocalDate.of(2025, 4, 1));
        EmployeeSalaryComponentValue comp = new EmployeeSalaryComponentValue();
        comp.setAmount(BigDecimal.valueOf(150000));
        assignment.setComponentValues(List.of(comp));

        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(10L), eq(101L), any(LocalDate.class)))
                .thenReturn(List.of(assignment));

        // Mock Performance
        PerformanceReviewRecord record = new PerformanceReviewRecord();
        record.setId(301L);
        record.setFinalScore(BigDecimal.valueOf(4.5));
        record.setApprovedAt(LocalDateTime.of(2025, 12, 1, 10, 0));
        when(reviewRecordRepository.findByOrganizationIdAndEmployeeId(10L, 101L))
                .thenReturn(List.of(record));

        // Mock Actions
        EmployeeActionDto actionDto = new EmployeeActionDto(
                "LEAVE-501", "LEAVE_APPROVAL", "Leave pending", "PENDING", "NORMAL", "2026-09-25",
                "Manager review required", new EmployeeActionLinkDto("View Task", "/employee/leave/501")
        );
        when(actionCenterService.getActionCenterResponse(employee, 10L))
                .thenReturn(new EmployeeActionCenterResponseDto(1, List.of(actionDto)));

        EmployeeDashboardResponse response = dashboardService.getDashboard();

        assertThat(response).isNotNull();
        assertThat(response.summary()).isNotNull();

        // Verify Attendance
        EmployeeAttendanceSummaryDto att = response.summary().attendance();
        assertThat(att.workingDays()).isEqualTo(24);
        assertThat(att.presentDays()).isEqualTo(22);
        assertThat(att.percentage()).isEqualTo(91.7);
        assertThat(att.trendDirection()).isEqualTo("UP");

        // Verify Leave Balance
        EmployeeLeaveBalanceSummaryDto leave = response.summary().leaveBalance();
        assertThat(leave.totalAvailable()).isEqualTo(10.0);
        assertThat(leave.balances()).containsEntry("CL", 6.0).containsEntry("EL", 4.0);

        // Verify Compensation
        EmployeeCompensationSummaryDto ctc = response.summary().compensation();
        assertThat(ctc.currentCtc()).isEqualByComparingTo(BigDecimal.valueOf(1800000));
        assertThat(ctc.currency()).isEqualTo("INR");
        assertThat(ctc.period()).isEqualTo("ANNUAL");
        assertThat(ctc.revisedDate()).isEqualTo("2025-04-01");

        // Verify Performance
        EmployeePerformanceSummaryDto perf = response.summary().performance();
        assertThat(perf.rating()).isEqualTo(4.5);
        assertThat(perf.ratingLabel()).isEqualTo("Excellent");
        assertThat(perf.lastReviewDate()).isEqualTo("2025-12-01");

        // Verify Pending Actions
        assertThat(response.pendingActions().total()).isEqualTo(1);
        assertThat(response.pendingActions().items()).hasSize(1);
        assertThat(response.pendingActions().items().get(0).id()).isEqualTo("LEAVE-501");
    }

    @Test
    void testGetAttendanceSummary_Success() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        MonthlyAttendanceCalendarResponse cal = new MonthlyAttendanceCalendarResponse();
        cal.setWorkingDays(24);
        cal.setPresentDays(22);
        when(attendanceCalendarService.getMonthlyCalendar(anyInt(), anyInt()))
                .thenReturn(cal);

        EmployeeAttendanceDetailSummaryDto result = dashboardService.getAttendanceSummary();

        assertThat(result).isNotNull();
        assertThat(result.workingDays()).isEqualTo(24);
        assertThat(result.presentDays()).isEqualTo(22);
        assertThat(result.attendancePercentage()).isEqualTo(91.7);
        assertThat(result.period().startDate()).contains("-01");
    }

    @Test
    void testGetAttendanceHistory_ScopedToEmployee() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        Attendance att = new Attendance();
        att.setId(99L);
        att.setEmployee(employee);
        att.setDate(LocalDate.of(2026, 9, 15));
        att.setStatus(AttendanceStatus.PRESENT);

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(101L), any(LocalDate.class), any(LocalDate.class), eq(10L)))
                .thenReturn(List.of(att));

        AttendanceCoreResponse coreResp = new AttendanceCoreResponse();
        coreResp.setAttendanceId(99L);
        coreResp.setStatus("PRESENT");
        when(attendanceService.mapToCoreResponse(att)).thenReturn(coreResp);

        List<AttendanceCoreResponse> history = dashboardService.getAttendanceHistory("2026-09");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getAttendanceId()).isEqualTo(99L);
    }

    @Test
    void testGetLeaveBalance_Success() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        LeaveType cl = new LeaveType();
        cl.setName("Casual Leave");
        LeaveBalance b1 = new LeaveBalance();
        b1.setLeaveType(cl);
        b1.setTotalEntitlement(10.0);
        b1.setUsedBalance(4.0);
        b1.setPendingBalance(0.0);

        when(leaveBalanceRepository.findByEmployeeId(101L)).thenReturn(List.of(b1));

        EmployeeLeaveBalanceDetailDto result = dashboardService.getLeaveBalance();
        assertThat(result.totalAvailable()).isEqualTo(6.0);
        assertThat(result.leaveTypes()).hasSize(1);
        assertThat(result.leaveTypes().get(0).code()).isEqualTo("CL");
        assertThat(result.leaveTypes().get(0).available()).isEqualTo(6.0);
    }

    @Test
    void testGetMyLeaveById_DeniedWhenAccessingAnotherEmployee() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        Employee otherEmp = new Employee();
        otherEmp.setId(999L);

        Leave otherLeave = new Leave();
        otherLeave.setId(888L);
        otherLeave.setEmployee(otherEmp);

        when(leaveService.getLeaveById(888L)).thenReturn(Optional.of(otherLeave));

        assertThatThrownBy(() -> dashboardService.getMyLeaveById(888L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only view your own leave requests");
    }

    @Test
    void testGetMyDocumentDetails_DeniedWhenAccessingAnotherEmployee() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        Employee otherEmp = new Employee();
        otherEmp.setId(999L);

        MyEmployeeDocument doc = new MyEmployeeDocument();
        doc.setId(777L);
        doc.setEmployee(otherEmp);

        when(employeeDocumentRepository.findById(777L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> dashboardService.getMyDocumentDetails(777L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only view your own documents");
    }

    @Test
    void testGetMyTrainingDetails_DeniedWhenNotAssigned() {
        when(employeeRepository.findByEmailAndOrganizationId("sarah.jenkins@acme.com", 10L))
                .thenReturn(Optional.of(employee));

        when(trainingAssignmentService.getAssignmentDetails(555L))
                .thenReturn(Optional.of(Map.of("id", 555L, "title", "Docker Basics")));

        when(progressRepository.findByAssignmentIdAndEmployeeId(555L, 101L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getMyTrainingDetails(555L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not assigned to this training course");
    }
}
