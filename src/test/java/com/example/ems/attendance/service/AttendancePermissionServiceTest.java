package com.example.ems.attendance.service;

import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.permission.AttendancePermissionCreateRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionRejectRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionResponse;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.repository.AttendancePermissionRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendancePermissionServiceTest {

    @Mock
    private AttendancePermissionRepository permissionRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendancePolicyService policyService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AttendanceGraceService graceService;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private AttendancePermissionService permissionService;

    private Organization organization;
    private Employee employee;
    private AttendancePolicy policy;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(10L);
        employee.setEmail("emp@org.com");
        employee.setOrganization(organization);

        policy = new AttendancePolicy();
        policy.setId(100L);
        policy.setName("Standard Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setMinimumWorkingMinutes(480);
        policy.setMaxMonthlyPermissions(4);
        policy.setMaxDailyPermissionMinutes(120);
        policy.setMaxMonthlyPermissionMinutes(480);
        policy.setGracePeriodType(GracePeriodType.MONTHLY);

        AuthPrincipal principal = new AuthPrincipal("10", "sess-1", 1, 1L, "emp@org.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create Permission: Success with valid quota")
    void testCreatePermission_Success() {
        AttendancePermissionCreateRequest req = new AttendancePermissionCreateRequest();
        req.setEmployeeId(10L);
        req.setAttendanceDate(LocalDate.of(2026, 9, 10));
        req.setPermissionType(AttendancePermissionType.LATE_ARRIVAL);
        req.setRequestedMinutes(30);
        req.setReason("Doctor appointment in the morning");

        when(employeeRepository.findByIdAndOrganizationId(10L, 1L)).thenReturn(Optional.of(employee));
        when(policyService.getActivePolicy(1L)).thenReturn(policy);
        when(graceService.calculatePeriodBounds(eq(req.getAttendanceDate()), any()))
                .thenReturn(new AttendanceGraceService.PeriodBounds(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
        when(permissionRepository.countPermissionsInPeriod(eq(1L), eq(10L), any(), any())).thenReturn(1L);
        when(permissionRepository.sumMinutesForDate(eq(1L), eq(10L), any())).thenReturn(0);
        when(permissionRepository.sumMinutesInPeriod(eq(1L), eq(10L), any(), any())).thenReturn(30);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(permissionRepository.save(any(AttendancePermission.class))).thenAnswer(inv -> {
            AttendancePermission p = inv.getArgument(0);
            p.setId(500L);
            return p;
        });

        AttendancePermissionResponse res = permissionService.createPermission(req);

        assertNotNull(res);
        assertEquals(500L, res.getId());
        assertEquals(AttendancePermissionStatus.PENDING, res.getStatus());
        assertEquals(AttendancePermissionType.LATE_ARRIVAL, res.getPermissionType());
        assertEquals(30, res.getRequestedMinutes());
    }

    @Test
    @DisplayName("Create Permission: Throws when daily limit exceeded")
    void testCreatePermission_DailyLimitExceeded() {
        AttendancePermissionCreateRequest req = new AttendancePermissionCreateRequest();
        req.setEmployeeId(10L);
        req.setAttendanceDate(LocalDate.of(2026, 9, 10));
        req.setPermissionType(AttendancePermissionType.LATE_ARRIVAL);
        req.setRequestedMinutes(90); // 90 + 60 = 150 > 120 max
        req.setReason("Long appointment");

        when(employeeRepository.findByIdAndOrganizationId(10L, 1L)).thenReturn(Optional.of(employee));
        when(policyService.getActivePolicy(1L)).thenReturn(policy);
        when(graceService.calculatePeriodBounds(any(), any()))
                .thenReturn(new AttendanceGraceService.PeriodBounds(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
        when(permissionRepository.countPermissionsInPeriod(any(), any(), any(), any())).thenReturn(0L);
        when(permissionRepository.sumMinutesForDate(any(), any(), any())).thenReturn(60);

        assertThrows(IllegalStateException.class, () -> permissionService.createPermission(req));
    }

    @Test
    @DisplayName("Approve Permission: Excuses late status and recalculates payable minutes without changing checkIn timestamp")
    void testApprovePermission_RecalculatesAttendance() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant actualPunch = date.atTime(9, 25).atZone(java.time.ZoneId.of("Asia/Kolkata")).toInstant();

        Attendance attendance = new Attendance();
        attendance.setId(88L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(date);
        attendance.setStatus(AttendanceStatus.LATE);
        attendance.setCheckInTime(actualPunch); // 09:25:00
        attendance.setPunchInTime(LocalTime.of(9, 25));
        attendance.setIsLate(true);
        attendance.setLateBy("00:25");
        attendance.setLateByMinutes(25);
        attendance.setLateStatus(AttendanceLateStatus.UNEXCUSED);
        attendance.setPayableMinutes(455); // 480 - 25

        AttendancePermission perm = new AttendancePermission();
        perm.setId(500L);
        perm.setOrganization(organization);
        perm.setEmployee(employee);
        perm.setAttendanceDate(date);
        perm.setPermissionType(AttendancePermissionType.LATE_ARRIVAL);
        perm.setRequestedMinutes(30);
        perm.setStatus(AttendancePermissionStatus.PENDING);

        when(permissionRepository.findByIdAndOrganizationId(500L, 1L)).thenReturn(Optional.of(perm));
        when(permissionRepository.save(any(AttendancePermission.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRepository.findByEmployeeIdAndDate(10L, date)).thenReturn(Optional.of(attendance));
        when(policyService.getActivePolicy(1L)).thenReturn(policy);

        AttendancePermissionResponse res = permissionService.approvePermission(500L, "Approved by Manager");

        assertNotNull(res);
        assertEquals(AttendancePermissionStatus.APPLIED, res.getStatus());

        // Verify Attendance changes
        assertEquals(AttendanceLateStatus.EXCUSED, attendance.getLateStatus());
        assertFalse(attendance.getIsLate());
        assertEquals(480, attendance.getPayableMinutes()); // Restored to full scheduled payable minutes
        assertEquals(actualPunch, attendance.getCheckInTime()); // NEVER modified original punch audit timestamp!
        assertEquals(LocalTime.of(9, 25), attendance.getPunchInTime());
    }

    @Test
    @DisplayName("Reject Permission: Status transitions to REJECTED with rejection reason")
    void testRejectPermission_Success() {
        AttendancePermission perm = new AttendancePermission();
        perm.setId(500L);
        perm.setOrganization(organization);
        perm.setStatus(AttendancePermissionStatus.PENDING);

        when(permissionRepository.findByIdAndOrganizationId(500L, 1L)).thenReturn(Optional.of(perm));
        when(permissionRepository.save(any(AttendancePermission.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendancePermissionResponse res = permissionService.rejectPermission(500L, new AttendancePermissionRejectRequest("Team meeting scheduled"));

        assertNotNull(res);
        assertEquals(AttendancePermissionStatus.REJECTED, res.getStatus());
        assertEquals("Team meeting scheduled", res.getRejectionReason());
    }
}
