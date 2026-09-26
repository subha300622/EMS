package com.example.ems.dashboard;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.dto.dashboard.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.actioncenter.EmployeeActionCenterService;
import com.example.ems.employee.service.impl.EmployeeDashboardServiceImpl;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import com.example.ems.payroll.entity.SalaryStructure;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeDashboardServiceTest {

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
    private PerformanceReviewRecordRepository reviewRecordRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private EmployeeActionCenterService actionCenterService;

    @InjectMocks
    private EmployeeDashboardServiceImpl dashboardService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john@company.com", "password", Collections.emptyList())
        );

        employee = new Employee();
        employee.setId(100L);
        employee.setEmail("john@company.com");
        com.example.ems.organization.entity.Organization org = new com.example.ems.organization.entity.Organization();
        org.setId(1L);
        employee.setOrganization(org);

        when(employeeRepository.findByEmail("john@company.com")).thenReturn(Optional.of(employee));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetDashboard_AggregatesAllSectionsSuccessfully() {
        // Attendance
        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(eq(100L), any(), any(), eq(1L)))
                .thenReturn(Collections.emptyList());

        // Leave Balances: CL 6, EL 4, SL 2
        LeaveType clType = new LeaveType(); clType.setName("Casual Leave");
        LeaveType elType = new LeaveType(); elType.setName("Earned Leave");
        LeaveType slType = new LeaveType(); slType.setName("Sick Leave");

        LeaveBalance b1 = new LeaveBalance(); b1.setLeaveType(clType); b1.setTotalEntitlement(6.0); b1.setUsedBalance(0.0); b1.setPendingBalance(0.0);
        LeaveBalance b2 = new LeaveBalance(); b2.setLeaveType(elType); b2.setTotalEntitlement(4.0); b2.setUsedBalance(0.0); b2.setPendingBalance(0.0);
        LeaveBalance b3 = new LeaveBalance(); b3.setLeaveType(slType); b3.setTotalEntitlement(2.0); b3.setUsedBalance(0.0); b3.setPendingBalance(0.0);

        when(leaveBalanceRepository.findByEmployeeId(100L))
                .thenReturn(List.of(b1, b2, b3));

        // Compensation: CTC 1,800,000 (monthly 150,000)
        EmployeeSalaryAssignment salaryAssignment = new EmployeeSalaryAssignment();
        salaryAssignment.setEffectiveFrom(LocalDate.of(2025, 4, 1));
        SalaryStructure structure = new SalaryStructure();
        structure.setCurrency("INR");
        salaryAssignment.setSalaryStructure(structure);

        EmployeeSalaryComponentValue componentValue = new EmployeeSalaryComponentValue();
        componentValue.setAmount(BigDecimal.valueOf(150000));
        salaryAssignment.setComponentValues(List.of(componentValue));

        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(1L), eq(100L), any()))
                .thenReturn(List.of(salaryAssignment));

        // Performance: 4.5
        when(reviewRecordRepository.findByOrganizationIdAndEmployeeId(1L, 100L))
                .thenReturn(Collections.emptyList());
        Appraisal appraisal = new Appraisal();
        appraisal.setFinalRating(4.5);
        when(appraisalRepository.findByOrganizationIdAndEmployeeId(1L, 100L))
                .thenReturn(List.of(appraisal));

        // Action Center: 0 pending
        when(actionCenterService.getActionCenterResponse(employee, 1L))
                .thenReturn(new EmployeeActionCenterResponseDto(0, Collections.emptyList()));

        EmployeeDashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertNotNull(response.summary());
        assertEquals(12.0, response.summary().leaveBalance().totalAvailable());
        assertEquals(BigDecimal.valueOf(1800000), response.summary().compensation().currentCtc());
        assertEquals("INR", response.summary().compensation().currency());
        assertEquals(4.5, response.summary().performance().rating());
        assertEquals(0, response.pendingActions().total());
    }

    @Test
    void testGetLeaveBalance_SumsAvailableBalancesCorrectly() {
        LeaveType clType = new LeaveType(); clType.setName("Casual Leave");
        LeaveType elType = new LeaveType(); elType.setName("Earned Leave");
        LeaveType slType = new LeaveType(); slType.setName("Sick Leave");

        LeaveBalance b1 = new LeaveBalance(); b1.setLeaveType(clType); b1.setTotalEntitlement(6.0); b1.setUsedBalance(0.0); b1.setPendingBalance(0.0);
        LeaveBalance b2 = new LeaveBalance(); b2.setLeaveType(elType); b2.setTotalEntitlement(4.0); b2.setUsedBalance(0.0); b2.setPendingBalance(0.0);
        LeaveBalance b3 = new LeaveBalance(); b3.setLeaveType(slType); b3.setTotalEntitlement(2.0); b3.setUsedBalance(0.0); b3.setPendingBalance(0.0);

        when(leaveBalanceRepository.findByEmployeeId(100L))
                .thenReturn(List.of(b1, b2, b3));

        EmployeeLeaveBalanceDetailDto balanceDto = dashboardService.getLeaveBalance();

        assertNotNull(balanceDto);
        assertEquals(12.0, balanceDto.totalAvailable());
        assertEquals(3, balanceDto.leaveTypes().size());
        assertEquals("CL", balanceDto.leaveTypes().get(0).code());
        assertEquals(6.0, balanceDto.leaveTypes().get(0).available());
        assertEquals("EL", balanceDto.leaveTypes().get(1).code());
        assertEquals(4.0, balanceDto.leaveTypes().get(1).available());
        assertEquals("SL", balanceDto.leaveTypes().get(2).code());
        assertEquals(2.0, balanceDto.leaveTypes().get(2).available());
    }

    @Test
    void testGetCurrentCompensation_ReturnsConfiguredCtc() {
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment();
        assignment.setEffectiveFrom(LocalDate.of(2025, 4, 1));
        SalaryStructure structure = new SalaryStructure();
        structure.setCurrency("INR");
        assignment.setSalaryStructure(structure);

        EmployeeSalaryComponentValue componentValue = new EmployeeSalaryComponentValue();
        componentValue.setAmount(BigDecimal.valueOf(150000));
        assignment.setComponentValues(List.of(componentValue));

        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(1L), eq(100L), any()))
                .thenReturn(List.of(assignment));

        EmployeeCompensationSummaryDto comp = dashboardService.getCurrentCompensation();

        assertNotNull(comp);
        assertEquals(BigDecimal.valueOf(1800000), comp.currentCtc());
        assertEquals("INR", comp.currency());
        assertEquals("2025-04-01", comp.revisedDate());
    }

    @Test
    void testGetCurrentCompensation_WithoutSalaryAssignment_ReturnsZeroDefault() {
        when(salaryAssignmentRepository.findActiveAssignmentsForDate(eq(1L), eq(100L), any()))
                .thenReturn(Collections.emptyList());
        when(salaryAssignmentRepository.findByOrganizationIdAndEmployeeIdOrderByEffectiveFromDesc(eq(1L), eq(100L)))
                .thenReturn(Collections.emptyList());
        when(payrollRepository.findByEmployeeId(100L))
                .thenReturn(Collections.emptyList());

        EmployeeCompensationSummaryDto comp = dashboardService.getCurrentCompensation();

        assertNotNull(comp);
        assertEquals(BigDecimal.ZERO, comp.currentCtc());
        assertEquals("INR", comp.currency());
    }

    @Test
    void testGetPerformanceSummary_ReturnsLatestRating() {
        when(reviewRecordRepository.findByOrganizationIdAndEmployeeId(1L, 100L))
                .thenReturn(Collections.emptyList());

        Appraisal a = new Appraisal();
        a.setId(10L);
        a.setFinalRating(4.8);
        when(appraisalRepository.findByOrganizationIdAndEmployeeId(1L, 100L))
                .thenReturn(List.of(a));

        EmployeePerformanceSummaryDto perf = dashboardService.getPerformanceSummary();

        assertNotNull(perf);
        assertEquals(4.8, perf.rating());
        assertEquals(5.0, perf.maxRating());
    }
}
