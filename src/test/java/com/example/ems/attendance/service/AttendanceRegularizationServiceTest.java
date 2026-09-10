package com.example.ems.attendance.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.CreateRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationApprovalRequest;
import com.example.ems.attendance.dto.RegularizationResponseDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceRegularizationServiceTest {

    @Mock
    private AttendanceRegularizationRepository regularizationRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private AttendanceRegularizationService regularizationService;

    private Employee employee;
    private Organization organization;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Acme Corp");

        employee = new Employee();
        employee.setId(125L);
        employee.setEmployeeId("EMP-125");
        employee.setFullName("John Doe");
        employee.setEmail("john@acme.com");
        employee.setOrganization(organization);

        attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T09:30:00Z"));
        attendance.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        attendance.setStatus(AttendanceStatus.COMPLETED);

        lenient().when(attendanceService.resolveCurrentEmployee()).thenReturn(employee);

        AuthPrincipal principal = new AuthPrincipal("125", "sess-1", 1, 100L, "john@acme.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getName()).thenReturn("john@acme.com");
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
    @DisplayName("Create Regularization: Both timestamps supplied and valid")
    void testCreateRegularization_BothTimestamps_Success() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(101L, 125L, 100L))
                .thenReturn(Optional.of(attendance));
        when(regularizationRepository.existsByAttendanceIdAndStatus(101L, AttendanceRegularizationStatus.PENDING))
                .thenReturn(false);
        when(regularizationRepository.save(any(AttendanceRegularization.class)))
                .thenAnswer(inv -> {
                    AttendanceRegularization r = inv.getArgument(0);
                    r.setId(501L);
                    return r;
                });

        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setId(99L);
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(instance);

        CreateRegularizationRequest req = new CreateRegularizationRequest(
                101L,
                Instant.parse("2026-09-10T09:00:00Z"),
                Instant.parse("2026-09-10T18:30:00Z"),
                "Forgot to swipe out on time"
        );

        RegularizationResponseDto response = regularizationService.createRegularization(req);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(101L, response.getAttendanceId());
        assertEquals(AttendanceRegularizationStatus.PENDING, response.getStatus());
        assertEquals("99", response.getWorkflowInstanceId());

        verify(regularizationRepository, atLeastOnce()).save(any(AttendanceRegularization.class));
        verify(approvalFacade).startApproval(any(ApprovalContext.class));
    }

    @Test
    @DisplayName("Create Regularization: Check-in only correction evaluated against existing check-out")
    void testCreateRegularization_CheckInOnly_Success() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(101L, 125L, 100L))
                .thenReturn(Optional.of(attendance));
        when(regularizationRepository.existsByAttendanceIdAndStatus(101L, AttendanceRegularizationStatus.PENDING))
                .thenReturn(false);
        when(regularizationRepository.save(any(AttendanceRegularization.class)))
                .thenAnswer(inv -> {
                    AttendanceRegularization r = inv.getArgument(0);
                    r.setId(502L);
                    return r;
                });

        CreateRegularizationRequest req = new CreateRegularizationRequest(
                101L,
                Instant.parse("2026-09-10T09:15:00Z"),
                null,
                "Correct check-in time"
        );

        RegularizationResponseDto response = regularizationService.createRegularization(req);

        assertNotNull(response);
        assertEquals(502L, response.getId());
        assertEquals(AttendanceRegularizationStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Create Regularization: Check-out only correction evaluated against existing check-in")
    void testCreateRegularization_CheckOutOnly_Success() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(101L, 125L, 100L))
                .thenReturn(Optional.of(attendance));
        when(regularizationRepository.existsByAttendanceIdAndStatus(101L, AttendanceRegularizationStatus.PENDING))
                .thenReturn(false);
        when(regularizationRepository.save(any(AttendanceRegularization.class)))
                .thenAnswer(inv -> {
                    AttendanceRegularization r = inv.getArgument(0);
                    r.setId(503L);
                    return r;
                });

        CreateRegularizationRequest req = new CreateRegularizationRequest(
                101L,
                null,
                Instant.parse("2026-09-10T19:00:00Z"),
                "Worked overtime"
        );

        RegularizationResponseDto response = regularizationService.createRegularization(req);

        assertNotNull(response);
        assertEquals(503L, response.getId());
        assertEquals(AttendanceRegularizationStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Create Regularization: Rejects if requested checkout is before checkin")
    void testCreateRegularization_InvalidTimestamps_ThrowsException() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(101L, 125L, 100L))
                .thenReturn(Optional.of(attendance));

        CreateRegularizationRequest req = new CreateRegularizationRequest(
                101L,
                Instant.parse("2026-09-10T19:00:00Z"),
                Instant.parse("2026-09-10T09:00:00Z"),
                "Wrong order"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> regularizationService.createRegularization(req));
        assertTrue(ex.getMessage().contains("Requested check-out time must be after check-in time"));
    }

    @Test
    @DisplayName("Create Regularization: Rejects when duplicate active PENDING request exists")
    void testCreateRegularization_DuplicatePending_ThrowsException() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(101L, 125L, 100L))
                .thenReturn(Optional.of(attendance));
        when(regularizationRepository.existsByAttendanceIdAndStatus(101L, AttendanceRegularizationStatus.PENDING))
                .thenReturn(true);

        CreateRegularizationRequest req = new CreateRegularizationRequest(
                101L,
                Instant.parse("2026-09-10T09:00:00Z"),
                Instant.parse("2026-09-10T18:00:00Z"),
                "Duplicate request"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> regularizationService.createRegularization(req));
        assertTrue(ex.getMessage().contains("An active pending regularization request already exists"));
    }

    // ── Test 1: Get own regularization ──────────────────────────────────────
    @Test
    @DisplayName("Test 1: Get own regularization - Returns 200 OK with employeeId, attendanceId, status")
    void testGetRegularizationById_OwnRequest_Success() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);
        reg.setDate(LocalDate.of(2026, 9, 10));

        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(501L, 125L, 100L))
                .thenReturn(Optional.of(reg));

        RegularizationResponseDto response = regularizationService.getRegularizationById(501L);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(125L, response.getEmployeeId());
        assertEquals(101L, response.getAttendanceId());
        assertEquals(AttendanceRegularizationStatus.PENDING, response.getStatus());
    }

    // ── Test 2: Try another employee's regularization ────────────────────────
    @Test
    @DisplayName("Test 2: Double-scoping barrier prevents reading other employee's regularization")
    void testGetById_OtherEmployee_ThrowsNotFound() {
        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(502L, 125L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> regularizationService.getRegularizationById(502L));
    }

    // ── Test 3: Cross-tenant regularization ──────────────────────────────────
    @Test
    @DisplayName("Test 3: Cross-tenant barrier prevents reading regularization from different tenant")
    void testGetById_CrossTenant_ThrowsNotFound() {
        // Current tenant context is 100L, query for org 9999 returns empty
        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(503L, 125L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> regularizationService.getRegularizationById(503L));
    }

    // ── Test 4: Approve pending request ──────────────────────────────────────
    @Test
    @DisplayName("Test 4: Approve pending request - Sets APPROVED and updates attendance timestamps")
    void testApproveRegularization_Success() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);
        reg.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        reg.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(regularizationRepository.findByIdAndOrganizationId(501L, 100L))
                .thenReturn(Optional.of(reg));
        when(regularizationRepository.save(any(AttendanceRegularization.class))).thenReturn(reg);

        RegularizationApprovalRequest req = new RegularizationApprovalRequest("Correction verified with building security log");
        RegularizationResponseDto res = regularizationService.approveRegularization(501L, req);

        assertNotNull(res);
        assertEquals(AttendanceRegularizationStatus.APPROVED, res.getStatus());
        verify(attendanceService).applyRegularizationCorrection(eq(101L), eq(Instant.parse("2026-09-10T09:00:00Z")), eq(Instant.parse("2026-09-10T18:00:00Z")));
    }

    // ── Test 5: Approve already approved request ─────────────────────────────
    @Test
    @DisplayName("Test 5: Approve already approved request - Throws IllegalStateException (idempotency/state protection)")
    void testApproveRegularization_AlreadyApproved_ThrowsException() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setStatus(AttendanceRegularizationStatus.APPROVED);

        when(regularizationRepository.findByIdAndOrganizationId(501L, 100L))
                .thenReturn(Optional.of(reg));

        RegularizationApprovalRequest req = new RegularizationApprovalRequest("Trying second approval");
        assertThrows(IllegalStateException.class, () -> regularizationService.approveRegularization(501L, req));

        verify(attendanceService, never()).applyRegularizationCorrection(any(), any(), any());
    }

    // ── Test 6: Reject pending request ───────────────────────────────────────
    @Test
    @DisplayName("Test 6: Reject pending request - Sets REJECTED and leaves attendance timestamps unchanged")
    void testRejectRegularization_Success_TimestampsUnchanged() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(504L);
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationRepository.findByIdAndOrganizationId(504L, 100L))
                .thenReturn(Optional.of(reg));
        when(regularizationRepository.save(any(AttendanceRegularization.class))).thenReturn(reg);

        RegularizationApprovalRequest req = new RegularizationApprovalRequest("Correction could not be verified");
        RegularizationResponseDto res = regularizationService.rejectRegularization(504L, req);

        assertNotNull(res);
        assertEquals(AttendanceRegularizationStatus.REJECTED, res.getStatus());
        assertEquals("Correction could not be verified", res.getRejectionReason());

        // CRITICAL: Attendance correction is NEVER invoked on rejection
        verify(attendanceService, never()).applyRegularizationCorrection(any(), any(), any());
    }

    // ── Test 7: Cancel by owner ──────────────────────────────────────────────
    @Test
    @DisplayName("Test 7: Cancel by owner - Transitions PENDING to CANCELLED")
    void testCancelRegularization_Success() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(505L);
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);
        reg.setWorkflowInstanceId("wf-1");

        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(505L, 125L, 100L))
                .thenReturn(Optional.of(reg));
        when(regularizationRepository.save(any(AttendanceRegularization.class))).thenReturn(reg);

        RegularizationResponseDto res = regularizationService.cancelRegularization(505L, "No longer required");

        assertNotNull(res);
        assertEquals(AttendanceRegularizationStatus.CANCELLED, res.getStatus());
        verify(approvalFacade).cancel(any(), any(), eq("505"), eq("No longer required"));
    }

    // ── Test 8: Another employee tries cancellation ──────────────────────────
    @Test
    @DisplayName("Test 8: Another employee tries cancellation - Double scoping blocks access with NotFound")
    void testCancelRegularization_OtherEmployee_ThrowsNotFound() {
        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(505L, 125L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> regularizationService.cancelRegularization(505L, "Sneaky cancel"));
    }

    // ── Test 9: Cancel approved request ──────────────────────────────────────
    @Test
    @DisplayName("Test 9: Cancel approved request - Fails because only PENDING requests can be cancelled")
    void testCancelRegularization_AlreadyApproved_ThrowsException() {
        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setStatus(AttendanceRegularizationStatus.APPROVED);

        when(regularizationRepository.findByIdAndEmployeeIdAndOrganizationId(501L, 125L, 100L))
                .thenReturn(Optional.of(reg));

        assertThrows(IllegalStateException.class, () -> regularizationService.cancelRegularization(501L, "Reason"));
    }

    // ── Test 10: Cross-tenant approve and reject ──────────────────────────────
    @Test
    @DisplayName("Test 10: Cross-tenant approve/reject - Organization barrier throws AttendanceNotFoundException")
    void testApproveAndReject_CrossTenant_ThrowsNotFound() {
        // Organization 100 querying for record in org 9999 returns empty
        when(regularizationRepository.findByIdAndOrganizationId(503L, 100L))
                .thenReturn(Optional.empty());

        RegularizationApprovalRequest req = new RegularizationApprovalRequest("Cross-tenant attempt");

        assertThrows(AttendanceNotFoundException.class, () -> regularizationService.approveRegularization(503L, req));
        assertThrows(AttendanceNotFoundException.class, () -> regularizationService.rejectRegularization(503L, req));

        verify(attendanceService, never()).applyRegularizationCorrection(any(), any(), any());
    }
}
