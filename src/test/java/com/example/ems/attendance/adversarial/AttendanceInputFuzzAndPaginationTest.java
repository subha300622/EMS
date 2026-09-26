package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.dto.late.LateAttendanceQuery;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceAdjustmentService;
import com.example.ems.attendance.service.AttendanceLateEarlyService;
import com.example.ems.attendance.service.AttendancePolicyService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing:
 * - 7. 🛡️ Input-Fuzz Testing
 * - 8. 📊 Pagination Testing
 * - 9. 🔎 Filter Combination Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceInputFuzzAndPaginationTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    @InjectMocks
    private AttendancePolicyService policyService;

    @InjectMocks
    private AttendanceLateEarlyService lateEarlyService;

    private Organization organization;
    private Employee employee;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-10");
        employee.setEmail("fuzz.tester@alphacorp.com");
        employee.setOrganization(organization);
        employee.setStatus("ACTIVE");

        attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        attendance.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── 1. Input-Fuzz Testing ────────────────────────────────────────────────

    @Test
    @DisplayName("Input-Fuzz: Null adjustment request payload is rejected with IllegalArgumentException")
    void testFuzz_NullAdjustmentRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adjustmentService.createAdjustment(101L, null));
    }

    @Test
    @DisplayName("Input-Fuzz: Null or empty reason in adjustment request is rejected")
    void testFuzz_EmptyReason_ThrowsException() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        req.setReason("   ");

        assertThrows(IllegalArgumentException.class, () -> adjustmentService.createAdjustment(101L, req));
    }

    @Test
    @DisplayName("Input-Fuzz: Both check-in and check-out timestamps null is rejected")
    void testFuzz_BothTimestampsNull_ThrowsException() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setReason("Valid reason");
        req.setRequestedCheckInTime(null);
        req.setRequestedCheckOutTime(null);

        assertThrows(IllegalArgumentException.class, () -> adjustmentService.createAdjustment(101L, req));
    }

    @Test
    @DisplayName("Input-Fuzz: Requested check-out timestamp before check-in timestamp is rejected")
    void testFuzz_CheckOutBeforeCheckIn_ThrowsException() {
        when(attendanceRepository.findByIdAndOrganizationId(101L, ORG_ID)).thenReturn(Optional.of(attendance));

        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setReason("Inverted time request");
        req.setRequestedCheckInTime(Instant.parse("2026-09-10T18:00:00Z"));
        req.setRequestedCheckOutTime(Instant.parse("2026-09-10T09:00:00Z"));

        assertThrows(IllegalArgumentException.class, () -> adjustmentService.createAdjustment(101L, req));
    }

    @Test
    @DisplayName("Input-Fuzz: Negative grace period on policy creation is rejected")
    void testFuzz_NegativeGracePeriod_ThrowsException() {
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        CreateAttendancePolicyRequest req = new CreateAttendancePolicyRequest();
        req.setName("Fuzz Policy");
        req.setOfficeStartTime(LocalTime.of(9, 0));
        req.setOfficeEndTime(LocalTime.of(18, 0));
        req.setGracePeriodMinutes(-100);

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
    }

    // ── 2. Pagination Testing ────────────────────────────────────────────────

    @Test
    @DisplayName("Pagination: Normal page bounds (page=0, size=20) pass correct Pageable")
    void testPagination_NormalBounds() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(adjustmentRepository.findAdjustments(eq(ORG_ID), isNull(), isNull(), isNull(), isNull(), captor.capture()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<AdjustmentResponseDto> result = adjustmentService.getAdjustments(null, null, null, null, 0, 20);
        assertNotNull(result);

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("Pagination: Zero size is safely normalized to default size (20)")
    void testPagination_ZeroSize_NormalizedToDefault() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(adjustmentRepository.findAdjustments(eq(ORG_ID), isNull(), isNull(), isNull(), isNull(), captor.capture()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        adjustmentService.getAdjustments(null, null, null, null, 0, 0);

        Pageable pageable = captor.getValue();
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("Pagination: Negative size is safely normalized to default size (20)")
    void testPagination_NegativeSize_NormalizedToDefault() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(adjustmentRepository.findAdjustments(eq(ORG_ID), isNull(), isNull(), isNull(), isNull(), captor.capture()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        adjustmentService.getAdjustments(null, null, null, null, 0, -50);

        Pageable pageable = captor.getValue();
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("Pagination: Oversized page size (100,000) is safely normalized to default/max limit")
    void testPagination_OversizedSize_NormalizedSafely() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(adjustmentRepository.findAdjustments(eq(ORG_ID), isNull(), isNull(), isNull(), isNull(), captor.capture()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        adjustmentService.getAdjustments(null, null, null, null, 0, 100000);

        Pageable pageable = captor.getValue();
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("Pagination: Extreme page index (page=999999) returns empty page safely without exception")
    void testPagination_ExtremePageIndex_ReturnsEmptySafely() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(adjustmentRepository.findAdjustments(eq(ORG_ID), isNull(), isNull(), isNull(), isNull(), captor.capture()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<AdjustmentResponseDto> result = adjustmentService.getAdjustments(null, null, null, null, 999999, 20);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        Pageable pageable = captor.getValue();
        assertEquals(999999, pageable.getPageNumber());
    }

    // ── 3. Filter Combination Testing ────────────────────────────────────────

    @Test
    @DisplayName("Filters: Late attendance report with fromDate > toDate throws IllegalArgumentException")
    void testFilter_FromDateAfterToDate_HandledSafely() {
        LateAttendanceQuery query = new LateAttendanceQuery();
        query.setFromDate(LocalDate.of(2026, 9, 20));
        query.setToDate(LocalDate.of(2026, 9, 10));

        assertThrows(IllegalArgumentException.class, () -> lateEarlyService.getLateAttendanceReport(query));
    }

    @Test
    @DisplayName("Filters: Adjustment query with employeeId + status + date range filters correctly")
    void testFilter_CombinedAdjustmentFilters() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 10);

        when(adjustmentRepository.findAdjustments(
                eq(ORG_ID),
                eq(10L),
                eq(AttendanceAdjustmentStatus.PENDING),
                eq(from),
                eq(to),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        var result = adjustmentService.getAdjustments(10L, AttendanceAdjustmentStatus.PENDING, from, to, 0, 20);
        assertNotNull(result);

        verify(adjustmentRepository).findAdjustments(
                eq(ORG_ID),
                eq(10L),
                eq(AttendanceAdjustmentStatus.PENDING),
                eq(from),
                eq(to),
                any(Pageable.class)
        );
    }
}
