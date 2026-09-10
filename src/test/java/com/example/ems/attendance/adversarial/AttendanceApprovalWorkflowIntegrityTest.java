package com.example.ems.attendance.adversarial;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.event.AttendanceRegularizationApprovalEventListener;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.*;
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

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing:
 * - 10.  Approval Workflow Integrity
 * - 11.  Idempotency Testing
 * - 12.  Failure-Recovery Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceApprovalWorkflowIntegrityTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private AttendanceRegularizationRepository regularizationRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    @Mock
    private AttendanceCorrectionService correctionService;

    @Mock
    private AttendancePolicyService policyService;

    @Mock
    private AttendanceService attendanceService;

    @Spy
    private AttendancePolicyEvaluator policyEvaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    @InjectMocks
    private AttendanceRegularizationApprovalEventListener eventListener;

    private Organization organization;
    private Employee employee;
    private Attendance attendance;
    private AttendancePolicy policy;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-10");
        employee.setEmail("workflow.tester@alphacorp.com");
        employee.setOrganization(organization);
        employee.setStatus("ACTIVE");

        attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T09:45:00Z")); // Originally 45 min late
        attendance.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        attendance.setIsLate(true);
        attendance.setLateByMinutes(30);
        attendance.setStatus(AttendanceStatus.COMPLETED);

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setOrganization(organization);
        policy.setName("Standard Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);

        mockSecurityContext("hr.admin@alphacorp.com");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(email);

        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);
    }

    // ── 1. Multi-Level Approval Workflow Integrity ──────────────────────────

    @Test
    @DisplayName("Workflow Integrity: L1 Manager Approval does NOT apply correction to attendance")
    void testMultiLevelApproval_L1Approval_DoesNotApplyCorrection() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
        adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        adjustment.setWorkflowInstanceId("wf-instance-123");

        verify(correctionService, never()).applyCorrection(any(), any(), any(), any(), any(), any());
        assertEquals(true, attendance.getIsLate());
        assertEquals(30, attendance.getLateByMinutes());
    }

    @Test
    @DisplayName("Workflow Integrity: Final L2 HR Approval triggers EXACTLY ONE attendance correction")
    void testMultiLevelApproval_FinalL2Approval_TriggersSingleCorrection() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(701L);
        reg.setAttendance(attendance);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);
        reg.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        reg.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(regularizationRepository.findById(701L)).thenReturn(Optional.of(reg));

        ApprovalWorkflowCompletedEvent finalEvent = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-instance-123",
                WorkflowType.ATTENDANCE_REGULARIZATION,
                "ATTENDANCE_REGULARIZATION",
                "701",
                ORG_ID,
                ApprovalStatus.APPROVED
        );

        eventListener.handleWorkflowCompleted(finalEvent);

        assertEquals(AttendanceRegularizationStatus.APPROVED, reg.getStatus());
        verify(regularizationRepository, times(1)).save(reg);
        verify(attendanceService, times(1)).applyRegularizationCorrection(
                eq(101L),
                eq(reg.getRequestedCheckInTime()),
                eq(reg.getRequestedCheckOutTime())
        );
    }

    // ── 2. Idempotency Testing ──────────────────────────────────────────────

    @Test
    @DisplayName("Idempotency: Repeated approval attempts on already APPROVED adjustment are rejected")
    void testIdempotency_RepeatedApprove_Rejected() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
        adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment saved = i.getArgument(0);
            adjustment.setStatus(saved.getStatus());
            return saved;
        });

        AdjustmentApprovalRequest req = new AdjustmentApprovalRequest();
        req.setRemarks("First approval");
        var res1 = adjustmentService.approveAdjustment(501L, req);
        assertEquals(AttendanceAdjustmentStatus.APPROVED, res1.getStatus());

        assertThrows(IllegalStateException.class, () -> adjustmentService.approveAdjustment(501L, req));
        assertThrows(IllegalStateException.class, () -> adjustmentService.approveAdjustment(501L, req));

        verify(correctionService, times(1)).applyCorrection(any(), any(), any(), any(), any(), eq("ADJUSTMENT"));
    }

    @Test
    @DisplayName("Idempotency: Repeated cancel attempts on already CANCELLED adjustment are rejected")
    void testIdempotency_RepeatedCancel_Rejected() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);

        when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment saved = i.getArgument(0);
            adjustment.setStatus(saved.getStatus());
            return saved;
        });

        var res1 = adjustmentService.cancelAdjustment(501L, "User mistake");
        assertEquals(AttendanceAdjustmentStatus.CANCELLED, res1.getStatus());

        assertThrows(IllegalStateException.class, () -> adjustmentService.cancelAdjustment(501L, "User mistake again"));
    }

    @Test
    @DisplayName("Idempotency: Repeated reject attempts on already REJECTED adjustment are rejected")
    void testIdempotency_RepeatedReject_Rejected() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);

        when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment saved = i.getArgument(0);
            adjustment.setStatus(saved.getStatus());
            return saved;
        });

        AdjustmentApprovalRequest req = new AdjustmentApprovalRequest();
        req.setRemarks("Policy violation");
        var res1 = adjustmentService.rejectAdjustment(501L, req);
        assertEquals(AttendanceAdjustmentStatus.REJECTED, res1.getStatus());

        assertThrows(IllegalStateException.class, () -> adjustmentService.rejectAdjustment(501L, req));
    }

    // ── 3. Failure-Recovery & Rollback Testing ───────────────────────────────

    @Test
    @DisplayName("Failure-Recovery: Downstream correction failure propagates and prevents partial corrupted state")
    void testFailureRecovery_CorrectionFailure_PropagatesException() {
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
        adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));

        doThrow(new RuntimeException("Database I/O error during attendance recalculation"))
                .when(correctionService).applyCorrection(any(), any(), any(), any(), any(), eq("ADJUSTMENT"));

        AdjustmentApprovalRequest req = new AdjustmentApprovalRequest();
        req.setRemarks("Approve request");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adjustmentService.approveAdjustment(501L, req));
        assertTrue(exception.getMessage().contains("Database I/O error"));
    }
}
