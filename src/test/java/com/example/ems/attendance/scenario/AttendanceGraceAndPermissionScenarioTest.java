package com.example.ems.attendance.scenario;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.dto.AttendanceDaySummaryDto;
import com.example.ems.attendance.dto.permission.AttendancePermissionResponse;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceGraceUsageRepository;
import com.example.ems.attendance.repository.AttendancePermissionRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceGraceAndPermissionScenarioTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AttendanceLogService attendanceLogService;

    @Mock
    private AttendancePolicyService attendancePolicyService;

    @Spy
    private AttendancePolicyEvaluator attendancePolicyEvaluator = new AttendancePolicyEvaluator();

    @Mock
    private AttendanceGraceService attendanceGraceService;

    @Mock
    private AttendanceGraceUsageRepository graceUsageRepository;

    @Mock
    private AttendancePermissionRepository attendancePermissionRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendancePermissionService permissionService;

    private Organization organization;
    private Employee employee;
    private AttendancePolicy policy;
    private ZoneId zone;

    @BeforeEach
    void setUp() {
        zone = ZoneId.of("Asia/Kolkata");
        TenantContext.clear();
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-100");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setOrganization(organization);
        employee.setStatus("ACTIVE");

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setName("Standard Office Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setMinimumWorkingMinutes(480);
        policy.setLateGraceMinutes(10);
        policy.setEarlyExitGraceMinutes(10);
        policy.setGraceOccurrencesPerPeriod(3);
        policy.setGracePeriodType(GracePeriodType.MONTHLY);
        policy.setAllowLateGrace(true);
        policy.setAllowEarlyExitGrace(true);
        policy.setMaxMonthlyPermissions(4);
        policy.setMaxDailyPermissionMinutes(120);
        policy.setMaxMonthlyPermissionMinutes(480);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);

        AuthPrincipal principal = new AuthPrincipal("10", "sess-1", 1, 1L, "john.doe@example.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(employeeRepository.findByEmailAndOrganizationId("john.doe@example.com", 1L))
                .thenReturn(Optional.of(employee));
        lenient().when(employeeRepository.findByIdAndOrganizationId(10L, 1L))
                .thenReturn(Optional.of(employee));
        lenient().when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(organization));
        lenient().when(attendancePolicyService.getActivePolicy(1L))
                .thenReturn(policy);

        ReflectionTestUtils.setField(permissionService, "policyService", attendancePolicyService);
        ReflectionTestUtils.setField(permissionService, "permissionRepository", attendancePermissionRepository);
        ReflectionTestUtils.setField(permissionService, "attendanceRepository", attendanceRepository);
        ReflectionTestUtils.setField(permissionService, "employeeRepository", employeeRepository);
        ReflectionTestUtils.setField(permissionService, "organizationRepository", organizationRepository);
        ReflectionTestUtils.setField(permissionService, "graceService", attendanceGraceService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Scenario 1: Check-in within grace period (09:07) -> GRACE_APPLIED, Payable Minutes 480, CheckIn unchanged")
    void testCheckIn_WithinGracePeriod() {
        LocalDate today = LocalDate.of(2026, 9, 11);
        Instant punchInstant = today.atTime(9, 7).atZone(zone).toInstant();
        Clock fixedClock = Clock.fixed(punchInstant, zone);
        ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);

        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(10L, today, 1L)).thenReturn(false);
        when(attendanceGraceService.evaluateLateGrace(1L, 10L, today, 7, policy))
                .thenReturn(new AttendanceGraceService.GraceEvaluationResult(true, 7, 1, false));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceCoreResponse res = attendanceService.checkInCore();

        assertNotNull(res);
        verify(attendanceGraceService).recordGraceUsage(1L, 10L, today, "LATE_ARRIVAL", 7, true, policy);
    }

    @Test
    @DisplayName("Scenario 2: Check-in beyond grace period (09:25) without permission -> UNEXCUSED, Late 25 mins, Payable 455")
    void testCheckIn_BeyondGraceWithoutPermission() {
        LocalDate today = LocalDate.of(2026, 9, 11);
        Instant punchInstant = today.atTime(9, 25).atZone(zone).toInstant();
        Clock fixedClock = Clock.fixed(punchInstant, zone);
        ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);

        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(10L, today, 1L)).thenReturn(false);
        when(attendanceGraceService.evaluateLateGrace(1L, 10L, today, 25, policy))
                .thenReturn(new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false));
        when(attendancePermissionRepository.findApprovedPermissions(1L, 10L, today, AttendancePermissionType.LATE_ARRIVAL))
                .thenReturn(Collections.emptyList());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceCoreResponse res = attendanceService.checkInCore();

        assertNotNull(res);
        verify(attendanceGraceService).recordGraceUsage(1L, 10L, today, "LATE_ARRIVAL", 25, false, policy);
    }

    @Test
    @DisplayName("Scenario 3: 09:25 Check-in with Approved Permission (30 mins) -> EXCUSED, Payable Minutes 480, Actual Punch 09:25 Unchanged")
    void testCheckIn_WithApprovedPermission() {
        LocalDate today = LocalDate.of(2026, 9, 11);
        Instant punchInstant = today.atTime(9, 25).atZone(zone).toInstant();
        Clock fixedClock = Clock.fixed(punchInstant, zone);
        ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);

        AttendancePermission approvedPerm = new AttendancePermission();
        approvedPerm.setId(101L);
        approvedPerm.setOrganization(organization);
        approvedPerm.setEmployee(employee);
        approvedPerm.setAttendanceDate(today);
        approvedPerm.setPermissionType(AttendancePermissionType.LATE_ARRIVAL);
        approvedPerm.setRequestedMinutes(30);
        approvedPerm.setStatus(AttendancePermissionStatus.APPROVED);

        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(10L, today, 1L)).thenReturn(false);
        when(attendanceGraceService.evaluateLateGrace(1L, 10L, today, 25, policy))
                .thenReturn(new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false));
        when(attendancePermissionRepository.findApprovedPermissions(1L, 10L, today, AttendancePermissionType.LATE_ARRIVAL))
                .thenReturn(List.of(approvedPerm));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceCoreResponse res = attendanceService.checkInCore();

        assertNotNull(res);
        assertEquals(punchInstant, res.getCheckInTime()); // Never changed punch timestamp!
    }

    @Test
    @DisplayName("Scenario 4: Permission Approved Retroactively -> Excuses Late and Restores 480 Payable Minutes")
    void testRetroactivePermissionApproval() {
        LocalDate today = LocalDate.of(2026, 9, 11);
        Instant punchInstant = today.atTime(9, 25).atZone(zone).toInstant();

        Attendance attendance = new Attendance();
        attendance.setId(55L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(today);
        attendance.setCheckInTime(punchInstant);
        attendance.setPunchInTime(LocalTime.of(9, 25));
        attendance.setIsLate(true);
        attendance.setLateBy("00:25");
        attendance.setLateByMinutes(25);
        attendance.setLateStatus(AttendanceLateStatus.UNEXCUSED);
        attendance.setPayableMinutes(455); // 480 - 25

        AttendancePermission permission = new AttendancePermission();
        permission.setId(200L);
        permission.setOrganization(organization);
        permission.setEmployee(employee);
        permission.setAttendanceDate(today);
        permission.setPermissionType(AttendancePermissionType.LATE_ARRIVAL);
        permission.setRequestedMinutes(30);
        permission.setStatus(AttendancePermissionStatus.PENDING);

        when(attendancePermissionRepository.findByIdAndOrganizationId(200L, 1L)).thenReturn(Optional.of(permission));
        when(attendancePermissionRepository.save(any(AttendancePermission.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRepository.findByEmployeeIdAndDate(10L, today)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendancePermissionResponse response = permissionService.approvePermission(200L, "Approved");

        assertNotNull(response);
        assertEquals(AttendancePermissionStatus.APPLIED, response.getStatus());
        assertEquals(AttendanceLateStatus.EXCUSED, attendance.getLateStatus());
        assertFalse(attendance.getIsLate());
        assertEquals(480, attendance.getPayableMinutes());
        assertEquals(punchInstant, attendance.getCheckInTime()); // Pure audit preservation
        assertEquals(LocalTime.of(9, 25), attendance.getPunchInTime());
    }

    @Test
    @DisplayName("Scenario 5: Day Summary combines audit timestamps, calculations, grace, permission, and payable minutes")
    void testGetAttendanceDaySummary() {
        LocalDate date = LocalDate.of(2026, 9, 11);
        Instant checkIn = date.atTime(9, 25).atZone(zone).toInstant();
        Instant checkOut = date.atTime(18, 0).atZone(zone).toInstant();

        Attendance attendance = new Attendance();
        attendance.setId(99L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(date);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setPunchInTime(LocalTime.of(9, 25));
        attendance.setPunchOutTime(LocalTime.of(18, 0));
        attendance.setLateByMinutes(25);
        attendance.setEarlyByMinutes(0);
        attendance.setLateStatus(AttendanceLateStatus.EXCUSED);
        attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.NONE);
        attendance.setGraceMinutes(0);
        attendance.setPermissionMinutes(30);
        attendance.setPayableMinutes(480);
        attendance.setTotalWorkingMinutes(515);
        attendance.setTotalBreakMinutes(0);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(10L, date, 1L))
                .thenReturn(Optional.of(attendance));

        AttendanceDaySummaryDto summary = attendanceService.getAttendanceDaySummary(10L, date);

        assertNotNull(summary);
        assertEquals(10L, summary.getEmployeeId());
        assertEquals(AttendanceStatus.PRESENT, summary.getStatus());
        assertEquals(AttendanceLateStatus.EXCUSED, summary.getLateStatus());
        assertEquals(480, summary.getPayableMinutes());
        assertEquals(checkIn, summary.getCheckInTime());
        assertEquals(checkOut, summary.getCheckOutTime());
        assertEquals(35, summary.getOvertimeMinutes()); // 515 - 480
    }
}
