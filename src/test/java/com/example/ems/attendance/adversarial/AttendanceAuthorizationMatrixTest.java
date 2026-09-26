package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.*;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 1. 🔐 Authorization Matrix Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceAuthorizationMatrixTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private AttendanceLogService attendanceLogService;

    @Mock
    private AttendancePolicyService policyService;

    @Mock
    private AttendanceCorrectionService correctionService;

    @Spy
    private AttendancePolicyEvaluator policyEvaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceService attendanceService;

    @InjectMocks
    private TeamAttendanceService teamAttendanceService;

    @InjectMocks
    private DepartmentAttendanceService departmentAttendanceService;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    private Clock fixedClock;
    private Organization organization;
    private Department engineeringDept;
    private Team backendTeam;
    private Employee empEmployee;
    private Employee empTeamLead;
    private Employee empManager;
    private Employee empDeptHead;
    private Employee empHrAdmin;
    private AttendancePolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);
        fixedClock = Clock.fixed(Instant.parse("2026-09-10T09:05:00Z"), ZoneId.of("UTC"));
        org.springframework.test.util.ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);
        org.springframework.test.util.ReflectionTestUtils.setField(teamAttendanceService, "clock", fixedClock);
        org.springframework.test.util.ReflectionTestUtils.setField(departmentAttendanceService, "clock", fixedClock);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        engineeringDept = new Department(10L, "Engineering", "ENG", "Engineering Dept");
        engineeringDept.setOrganization(organization);

        backendTeam = new Team();
        backendTeam.setId(20L);
        backendTeam.setTeamName("Backend Team");
        backendTeam.setTeamCode("BE-TEAM");
        backendTeam.setOrganization(organization);
        backendTeam.setDepartment(engineeringDept);

        empEmployee = createEmployee(1L, "emp@alphacorp.com", "ROLE_EMPLOYEE", engineeringDept);
        empTeamLead = createEmployee(2L, "tl@alphacorp.com", "ROLE_TEAM_LEAD", engineeringDept);
        empManager = createEmployee(3L, "mgr@alphacorp.com", "ROLE_MANAGER", engineeringDept);
        empDeptHead = createEmployee(4L, "depthead@alphacorp.com", "ROLE_DEPT_HEAD", engineeringDept);
        empHrAdmin = createEmployee(5L, "hradmin@alphacorp.com", "ROLE_HR_ADMIN", engineeringDept);

        defaultPolicy = new AttendancePolicy();
        defaultPolicy.setId(1L);
        defaultPolicy.setOrganization(organization);
        defaultPolicy.setName("Standard Policy");
        defaultPolicy.setOfficeStartTime(LocalTime.of(9, 0));
        defaultPolicy.setOfficeEndTime(LocalTime.of(18, 0));
        defaultPolicy.setGracePeriodMinutes(15);
        defaultPolicy.setStatus(AttendancePolicyStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private Employee createEmployee(Long id, String email, String role, Department dept) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeId("EMP-" + id);
        e.setEmail(email);
        e.setFirstName(role);
        e.setLastName("User");
        e.setDepartment(dept != null ? dept.getName() : null);
        e.setOrganization(organization);
        e.setStatus("ACTIVE");
        return e;
    }

    private void mockSecurityUser(Employee employee, String... roles) {
        AuthPrincipal principal = mock(AuthPrincipal.class);
        when(principal.getEmail()).thenReturn(employee.getEmail());
        when(principal.getUserId()).thenReturn(employee.getEmployeeId());

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);
        when(auth.getName()).thenReturn(employee.getEmail());

        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        lenient().when(employeeRepository.findByEmailAndOrganizationId(employee.getEmail(), ORG_ID))
                .thenReturn(Optional.of(employee));
    }

    // ── 1. Own Check-In/Out Allowed for All Roles ────────────────────────────

    @Test
    @DisplayName("Auth Matrix: Own check-in succeeds for Employee, Team Lead, Manager, Dept Head, HR/Admin")
    void testOwnCheckIn_AllowedForAllRoles() {
        List<Employee> allRoles = List.of(empEmployee, empTeamLead, empManager, empDeptHead, empHrAdmin);

        for (Employee actor : allRoles) {
            mockSecurityUser(actor);
            when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(eq(actor.getId()), any(LocalDate.class), eq(ORG_ID)))
                    .thenReturn(false);
            when(policyService.getActivePolicy(ORG_ID)).thenReturn(defaultPolicy);
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
                Attendance att = i.getArgument(0);
                att.setId(1000L + actor.getId());
                return att;
            });

            assertDoesNotThrow(() -> {
                var response = attendanceService.checkInCore();
                assertNotNull(response);
                assertEquals(actor.getId(), response.getEmployeeId());
            }, "Check-in should be permitted for " + actor.getEmail());
        }
    }

    @Test
    @DisplayName("Auth Matrix: Own attendance history retrieval allowed for all roles")
    void testOwnAttendanceHistory_AllowedForAllRoles() {
        List<Employee> allRoles = List.of(empEmployee, empTeamLead, empManager, empDeptHead, empHrAdmin);

        for (Employee actor : allRoles) {
            mockSecurityUser(actor);
            when(attendanceRepository.findHistory(
                    eq(actor.getId()),
                    eq(ORG_ID),
                    isNull(LocalDate.class),
                    isNull(LocalDate.class),
                    isNull(AttendanceStatus.class),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(Collections.emptyList()));

            assertDoesNotThrow(() -> {
                var query = new com.example.ems.attendance.dto.AttendanceHistoryQuery();
                var result = attendanceService.getAttendanceHistory(query);
                assertNotNull(result);
            });
        }
    }

    // ── 2. Team Attendance Authorization ────────────────────────────────────

    @Test
    @DisplayName("Auth Matrix: Team Attendance permitted for Team Lead, Manager, Dept Head, HR Admin")
    void testTeamAttendance_PermittedRoles() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(20L, ORG_ID))
                .thenReturn(Optional.of(backendTeam));
        TeamMember tm1 = new TeamMember(backendTeam, empEmployee, LocalDate.of(2026, 1, 1), true);
        when(teamMemberRepository.findByTeamIdAndStatus(20L, "ACTIVE"))
                .thenReturn(List.of(tm1));
        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(any(), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Collections.emptyList());
        when(leaveRepository.findByEmployeeIdInAndStatus(any(), eq("APPROVED")))
                .thenReturn(Collections.emptyList());

        // Team Lead
        mockSecurityUser(empTeamLead, "ROLE_TEAM_LEAD");
        var tlResponse = teamAttendanceService.getTeamDailyAttendance(20L, LocalDate.now(fixedClock), null);
        assertNotNull(tlResponse);
        assertEquals(20L, tlResponse.getTeamId());

        // Dept Head
        mockSecurityUser(empDeptHead, "ROLE_DEPT_HEAD");
        var dhResponse = teamAttendanceService.getTeamDailyAttendance(20L, LocalDate.now(fixedClock), null);
        assertNotNull(dhResponse);

        // HR Admin
        mockSecurityUser(empHrAdmin, "ROLE_HR_ADMIN");
        var hrResponse = teamAttendanceService.getTeamDailyAttendance(20L, LocalDate.now(fixedClock), null);
        assertNotNull(hrResponse);
    }

    // ── 3. Department Attendance Authorization ──────────────────────────────

    @Test
    @DisplayName("Auth Matrix: Department Attendance permitted for Dept Head and HR Admin")
    void testDepartmentAttendance_PermittedRoles() {
        when(departmentRepository.findByIdAndOrganizationId(10L, ORG_ID))
                .thenReturn(Optional.of(engineeringDept));
        when(employeeRepository.findByOrganizationIdAndDepartment(ORG_ID, "Engineering"))
                .thenReturn(new ArrayList<>(List.of(empEmployee, empTeamLead)));
        when(teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(10L, ORG_ID))
                .thenReturn(List.of(backendTeam));
        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(any(), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Collections.emptyList());
        when(leaveRepository.findByEmployeeIdInAndStatus(any(), eq("APPROVED")))
                .thenReturn(Collections.emptyList());

        // Dept Head
        mockSecurityUser(empDeptHead, "ROLE_DEPT_HEAD");
        var dhResponse = departmentAttendanceService.getDepartmentDailyAttendance(10L, LocalDate.now(fixedClock), null);
        assertNotNull(dhResponse);
        assertEquals(10L, dhResponse.getDepartmentId());

        // HR Admin
        mockSecurityUser(empHrAdmin, "ROLE_HR_ADMIN");
        var hrResponse = departmentAttendanceService.getDepartmentDailyAttendance(10L, LocalDate.now(fixedClock), null);
        assertNotNull(hrResponse);
    }

    // ── 4. Adjustment Creation (Own) vs Non-Owned ────────────────────────────

    @Test
    @DisplayName("Auth Matrix: Any employee can submit adjustment for their OWN attendance")
    void testAdjustmentCreation_AllowedForOwnAttendance() {
        mockSecurityUser(empEmployee, "ROLE_EMPLOYEE");

        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(empEmployee);
        attendance.setOrganization(organization);
        attendance.setCheckInTime(Instant.parse("2026-09-10T09:30:00Z"));
        attendance.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(attendanceRepository.findByIdAndOrganizationId(101L, ORG_ID)).thenReturn(Optional.of(attendance));
        when(adjustmentRepository.existsByAttendanceIdAndStatus(101L, AttendanceAdjustmentStatus.PENDING)).thenReturn(false);
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment adj = i.getArgument(0);
            adj.setId(501L);
            return adj;
        });

        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        req.setReason("Traffic delay adjustment");

        var response = adjustmentService.createAdjustment(101L, req);
        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(AttendanceAdjustmentStatus.PENDING, response.getStatus());
    }

    // ── 5. Adjustment Approval Authorization ─────────────────────────────────

    @Test
    @DisplayName("Auth Matrix: Adjustment Approval permitted for Team Lead, Manager, Dept Head, HR Admin")
    void testAdjustmentApproval_PermittedForSupervisorsAndHR() {
        List<Employee> approvers = List.of(empTeamLead, empManager, empDeptHead, empHrAdmin);

        for (Employee approver : approvers) {
            mockSecurityUser(approver);

            Attendance attendance = new Attendance();
            attendance.setId(101L);
            attendance.setEmployee(empEmployee);
            attendance.setOrganization(organization);

            AttendanceAdjustment adjustment = new AttendanceAdjustment();
            adjustment.setId(501L);
            adjustment.setAttendance(attendance);
            adjustment.setEmployee(empEmployee);
            adjustment.setOrganization(organization);
            adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
            adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
            adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

            when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenReturn(Optional.of(adjustment));
            when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));

            AdjustmentApprovalRequest approvalReq = new AdjustmentApprovalRequest();
            approvalReq.setRemarks("Approved by " + approver.getEmail());

            var result = adjustmentService.approveAdjustment(501L, approvalReq);
            assertNotNull(result);
            assertEquals(AttendanceAdjustmentStatus.APPROVED, result.getStatus());
            verify(correctionService, atLeastOnce()).applyCorrection(
                    eq(attendance),
                    eq(adjustment.getRequestedCheckInTime()),
                    eq(adjustment.getRequestedCheckOutTime()),
                    anyString(),
                    eq(approver.getEmail()),
                    eq("ADJUSTMENT")
            );
        }
    }
}
