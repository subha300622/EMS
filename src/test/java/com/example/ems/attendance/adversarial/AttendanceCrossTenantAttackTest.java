package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.dto.policy.UpdateAttendancePolicyRequest;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 6. 🏢 Cross-Tenant Attack Matrix
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceCrossTenantAttackTest {

    private static final Long TENANT_A_ORG_ID = 100L;

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceCorrectionService correctionService;

    @Mock
    private AttendancePolicyService attendancePolicyService;

    @Spy
    private AttendancePolicyEvaluator evaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendancePolicyService policyService;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    @InjectMocks
    private TeamAttendanceService teamAttendanceService;

    @InjectMocks
    private DepartmentAttendanceService departmentAttendanceService;

    @InjectMocks
    private AttendanceLateEarlyService lateEarlyService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);
        fixedClock = Clock.fixed(Instant.parse("2026-09-10T09:00:00Z"), ZoneId.of("UTC"));
        org.springframework.test.util.ReflectionTestUtils.setField(teamAttendanceService, "clock", fixedClock);
        org.springframework.test.util.ReflectionTestUtils.setField(departmentAttendanceService, "clock", fixedClock);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    // ── 1. Policy Cross-Tenant Attacks ──────────────────────────────────────

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to GET Tenant B policy is blocked (404)")
    void testTenantA_GetTenantBPolicy_ThrowsNotFound() {
        when(policyRepository.findByIdAndOrganizationId(999L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> policyService.getPolicyById(999L));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to UPDATE Tenant B policy is blocked (404)")
    void testTenantA_UpdateTenantBPolicy_ThrowsNotFound() {
        when(policyRepository.findByIdAndOrganizationId(999L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        UpdateAttendancePolicyRequest req = new UpdateAttendancePolicyRequest();
        req.setName("Malicious Override");

        assertThrows(AttendanceNotFoundException.class, () -> policyService.updatePolicy(999L, req));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to ACTIVATE Tenant B policy is blocked (404)")
    void testTenantA_ActivateTenantBPolicy_ThrowsNotFound() {
        when(policyRepository.findByIdAndOrganizationId(999L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> policyService.activatePolicy(999L));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to DEACTIVATE Tenant B policy is blocked (404)")
    void testTenantA_DeactivateTenantBPolicy_ThrowsNotFound() {
        when(policyRepository.findByIdAndOrganizationId(999L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> policyService.deactivatePolicy(999L));
    }

    // ── 2. Adjustment Cross-Tenant Attacks ──────────────────────────────────

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to GET Tenant B adjustment is blocked (404)")
    void testTenantA_GetTenantBAdjustment_ThrowsNotFound() {
        when(adjustmentRepository.findByIdAndOrganizationId(888L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.getAdjustmentById(888L));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to POST adjustment against Tenant B attendance is blocked (404)")
    void testTenantA_CreateAdjustmentAgainstTenantBAttendance_ThrowsNotFound() {
        when(attendanceRepository.findByIdAndOrganizationId(777L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        req.setReason("Malicious Tenant Infiltration");

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.createAdjustment(777L, req));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to APPROVE Tenant B adjustment is blocked (404)")
    void testTenantA_ApproveTenantBAdjustment_ThrowsNotFound() {
        when(adjustmentRepository.findByIdAndOrganizationId(888L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.approveAdjustment(888L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to REJECT Tenant B adjustment is blocked (404)")
    void testTenantA_RejectTenantBAdjustment_ThrowsNotFound() {
        when(adjustmentRepository.findByIdAndOrganizationId(888L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.rejectAdjustment(888L, new AdjustmentApprovalRequest()));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to CANCEL Tenant B adjustment is blocked (404)")
    void testTenantA_CancelTenantBAdjustment_ThrowsNotFound() {
        when(adjustmentRepository.findByIdAndOrganizationId(888L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.cancelAdjustment(888L, "Cancel attempt"));
    }

    // ── 3. Team & Department Cross-Tenant Attacks ───────────────────────────

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to GET Tenant B Team attendance is blocked (404)")
    void testTenantA_GetTenantBTeamAttendance_ThrowsNotFound() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(666L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> teamAttendanceService.getTeamDailyAttendance(666L, LocalDate.now(fixedClock), null));
    }

    @Test
    @DisplayName("Cross-Tenant: Tenant A attempting to GET Tenant B Department attendance is blocked (404)")
    void testTenantA_GetTenantBDepartmentAttendance_ThrowsNotFound() {
        when(departmentRepository.findByIdAndOrganizationId(555L, TENANT_A_ORG_ID)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> departmentAttendanceService.getDepartmentDailyAttendance(555L, LocalDate.now(fixedClock), null));
    }

    // ── 4. Late/Early Reports Cross-Tenant Scoping ──────────────────────────

    @Test
    @DisplayName("Cross-Tenant: Late report strictly passes authenticated Tenant A orgId into repository query")
    void testTenantA_LateReport_StrictlyScopedToTenantA() {
        when(attendanceRepository.findLateAttendance(
                eq(TENANT_A_ORG_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        var result = lateEarlyService.getLateAttendanceReport(new com.example.ems.attendance.dto.late.LateAttendanceQuery());
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(attendanceRepository).findLateAttendance(
                eq(TENANT_A_ORG_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }
}
