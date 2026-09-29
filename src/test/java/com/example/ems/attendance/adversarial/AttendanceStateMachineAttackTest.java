package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.exception.*;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceAdjustmentService;
import com.example.ems.attendance.service.AttendanceCorrectionService;
import com.example.ems.attendance.service.AttendancePolicyEvaluator;
import com.example.ems.attendance.service.AttendancePolicyService;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 3. 🔄 State-Machine Attack Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceStateMachineAttackTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePolicyService policyService;

    @Mock
    private AttendanceCorrectionService correctionService;

    @Mock
    private com.example.ems.attendance.service.AttendanceLogService attendanceLogService;

    @Spy
    private AttendancePolicyEvaluator policyEvaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    private Clock fixedClock;
    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);
        fixedClock = Clock.fixed(Instant.parse("2026-09-10T09:00:00Z"), ZoneId.of("UTC"));
        org.springframework.test.util.ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-10");
        employee.setEmail("state.attacker@alphacorp.com");
        employee.setOrganization(organization);
        employee.setStatus("ACTIVE");

        mockSecurityContext(employee);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(Employee emp) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(emp.getEmail());
        when(auth.getPrincipal()).thenReturn(emp.getEmail());

        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        lenient().when(employeeRepository.findByEmailAndOrganizationId(emp.getEmail(), ORG_ID))
                .thenReturn(Optional.of(emp));
    }

    // ── 1. Attendance Lifecycle State Attacks ────────────────────────────────

    @Test
    @DisplayName("State Attack: Check-Out when NOT_CHECKED_IN must fail")
    void testCheckOut_WhenNotCheckedIn_ThrowsException() {
        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.checkOutCore());
    }

    @Test
    @DisplayName("State Attack: Check-In when already WORKING / CHECKED_IN must fail")
    void testCheckIn_WhenAlreadyWorking_ThrowsDuplicateCheckIn() {
        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(true);

        assertThrows(DuplicateCheckInException.class, () -> attendanceService.checkInCore());
    }

    @Test
    @DisplayName("State Attack: Start Break when already COMPLETED (Checked-Out) must fail")
    void testStartBreak_WhenAlreadyCompleted_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.COMPLETED);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.startBreakCore());
    }

    @Test
    @DisplayName("State Attack: Check-Out when already COMPLETED must fail")
    void testCheckOut_WhenAlreadyCompleted_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.COMPLETED);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.checkOutCore());
    }

    @Test
    @DisplayName("State Attack: End Break when already COMPLETED must fail")
    void testEndBreak_WhenAlreadyCompleted_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.COMPLETED);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.endBreakCore());
    }

    @Test
    @DisplayName("State Attack: Start Break when already ON_BREAK must fail")
    void testStartBreak_WhenAlreadyOnBreak_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(ActiveBreakExistsException.class, () -> attendanceService.startBreakCore());
    }

    @Test
    @DisplayName("State Attack: Check-Out while ON_BREAK without ending break must fail")
    void testCheckOut_WhileOnBreak_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.checkOutCore());
    }

    @Test
    @DisplayName("State Attack: End Break when in WORKING state (no active break) must fail")
    void testEndBreak_WhenWorking_ThrowsException() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setStatus(AttendanceStatus.WORKING);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.endBreakCore());
    }

    @Test
    @DisplayName("State Attack: Check-In by TERMINATED / INACTIVE employee must fail")
    void testCheckIn_WhenEmployeeTerminated_ThrowsException() {
        employee.setStatus("TERMINATED");

        assertThrows(EmployeeNotActiveException.class, () -> attendanceService.checkInCore());
    }

    // ── 2. Adjustment State-Machine Attacks ──────────────────────────────────

    private AttendanceAdjustment createAdjustmentWithStatus(AttendanceAdjustmentStatus status) {
        Attendance att = new Attendance();
        att.setId(201L);
        att.setOrganization(organization);
        att.setEmployee(employee);

        AttendanceAdjustment adj = new AttendanceAdjustment();
        adj.setId(601L);
        adj.setAttendance(att);
        adj.setEmployee(employee);
        adj.setOrganization(organization);
        adj.setStatus(status);
        adj.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        adj.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        return adj;
    }

    @Test
    @DisplayName("Adjustment Attack: APPROVED -> APPROVE must be rejected")
    void testAdjustmentAttack_ApprovedToApprove_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.APPROVED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.approveAdjustment(601L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Adjustment Attack: APPROVED -> REJECT must be rejected")
    void testAdjustmentAttack_ApprovedToReject_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.APPROVED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.rejectAdjustment(601L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Adjustment Attack: APPROVED -> CANCEL must be rejected")
    void testAdjustmentAttack_ApprovedToCancel_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.APPROVED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.cancelAdjustment(601L, "Cancel test"));
    }

    @Test
    @DisplayName("Adjustment Attack: REJECTED -> APPROVE must be rejected")
    void testAdjustmentAttack_RejectedToApprove_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.REJECTED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.approveAdjustment(601L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Adjustment Attack: REJECTED -> REJECT again must be rejected")
    void testAdjustmentAttack_RejectedToReject_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.REJECTED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.rejectAdjustment(601L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Adjustment Attack: CANCELLED -> APPROVE must be rejected")
    void testAdjustmentAttack_CancelledToApprove_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.CANCELLED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.approveAdjustment(601L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Adjustment Attack: CANCELLED -> CANCEL again must be rejected")
    void testAdjustmentAttack_CancelledToCancel_Fails() {
        AttendanceAdjustment adj = createAdjustmentWithStatus(AttendanceAdjustmentStatus.CANCELLED);
        when(adjustmentRepository.findByIdAndOrganizationId(601L, ORG_ID)).thenReturn(Optional.of(adj));

        assertThrows(IllegalStateException.class, () -> adjustmentService.cancelAdjustment(601L, "Cancel again"));
    }
}
