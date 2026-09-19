package com.example.ems.attendance.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceAdjustment;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceAdjustmentServiceTest {

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceCorrectionService attendanceCorrectionService;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    private Organization organization;
    private Employee employee;
    private Attendance attendance;
    private AttendanceAdjustment adjustment;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);

        employee = new Employee();
        employee.setId(101L);
        employee.setFullName("Bob Jones");
        employee.setEmail("bob@example.com");

        attendance = new Attendance();
        attendance.setId(301L);
        attendance.setOrganization(organization);
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T03:30:00Z"));
        attendance.setCheckOutTime(Instant.parse("2026-09-10T11:00:00Z"));
        attendance.setStatus(AttendanceStatus.PRESENT);

        adjustment = new AttendanceAdjustment();
        adjustment.setId(401L);
        adjustment.setOrganization(organization);
        adjustment.setEmployee(employee);
        adjustment.setAttendance(attendance);
        adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T03:30:00Z"));
        adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T12:30:00Z"));
        adjustment.setReason("Extended working hours for release deployment");
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Adjustment: Success creates adjustment request in PENDING state")
    void testCreateAdjustment_Success() {
        CreateAdjustmentRequest request = new CreateAdjustmentRequest(
                Instant.parse("2026-09-10T03:30:00Z"),
                Instant.parse("2026-09-10T12:30:00Z"),
                "Extended shift"
        );

        when(attendanceRepository.findByIdAndOrganizationId(301L, 100L)).thenReturn(Optional.of(attendance));
        when(adjustmentRepository.existsByAttendanceIdAndStatus(301L, AttendanceAdjustmentStatus.PENDING)).thenReturn(false);
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment adj = i.getArgument(0);
            adj.setId(401L);
            return adj;
        });

        AdjustmentResponseDto result = adjustmentService.createAdjustment(301L, request);

        assertNotNull(result);
        assertEquals(401L, result.getId());
        assertEquals(AttendanceAdjustmentStatus.PENDING, result.getStatus());
        assertEquals("Extended shift", result.getReason());
        verify(adjustmentRepository, atLeastOnce()).save(any(AttendanceAdjustment.class));
    }

    @Test
    @DisplayName("Create Adjustment: Throws IllegalStateException when pending adjustment already exists")
    void testCreateAdjustment_DuplicatePending_ThrowsException() {
        CreateAdjustmentRequest request = new CreateAdjustmentRequest(
                Instant.parse("2026-09-10T03:30:00Z"),
                Instant.parse("2026-09-10T12:30:00Z"),
                "Extended shift"
        );

        when(attendanceRepository.findByIdAndOrganizationId(301L, 100L)).thenReturn(Optional.of(attendance));
        when(adjustmentRepository.existsByAttendanceIdAndStatus(301L, AttendanceAdjustmentStatus.PENDING)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> adjustmentService.createAdjustment(301L, request));
        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Approve Adjustment: Sets status to APPROVED and delegates to AttendanceCorrectionService")
    void testApproveAdjustment_Success() {
        AdjustmentApprovalRequest request = new AdjustmentApprovalRequest("Approved by manager");

        when(adjustmentRepository.findByIdAndOrganizationId(401L, 100L)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));

        AdjustmentResponseDto result = adjustmentService.approveAdjustment(401L, request);

        assertNotNull(result);
        assertEquals(AttendanceAdjustmentStatus.APPROVED, result.getStatus());
        assertEquals("Approved by manager", result.getManagerNotes());
        verify(attendanceCorrectionService, times(1)).applyCorrection(
                eq(attendance),
                eq(adjustment.getRequestedCheckInTime()),
                eq(adjustment.getRequestedCheckOutTime()),
                eq("Approved by manager"),
                anyString(),
                eq("ADJUSTMENT")
        );
    }

    @Test
    @DisplayName("Reject Adjustment: Sets status to REJECTED without modifying attendance")
    void testRejectAdjustment_Success() {
        AdjustmentApprovalRequest request = new AdjustmentApprovalRequest("Insufficient proof");

        when(adjustmentRepository.findByIdAndOrganizationId(401L, 100L)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));

        AdjustmentResponseDto result = adjustmentService.rejectAdjustment(401L, request);

        assertNotNull(result);
        assertEquals(AttendanceAdjustmentStatus.REJECTED, result.getStatus());
        assertEquals("Insufficient proof", result.getRejectionReason());
        verify(attendanceCorrectionService, never()).applyCorrection(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Cancel Adjustment: Sets status to CANCELLED")
    void testCancelAdjustment_Success() {
        adjustment.setWorkflowInstanceId("wf-101");
        when(adjustmentRepository.findByIdAndOrganizationId(401L, 100L)).thenReturn(Optional.of(adjustment));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));

        AdjustmentResponseDto result = adjustmentService.cancelAdjustment(401L, "Cancelled by user");

        assertNotNull(result);
        assertEquals(AttendanceAdjustmentStatus.CANCELLED, result.getStatus());
        verify(approvalFacade, times(1)).cancel(eq(WorkflowType.ATTENDANCE_ADJUSTMENT), anyString(), anyString(), anyString());
    }
}
