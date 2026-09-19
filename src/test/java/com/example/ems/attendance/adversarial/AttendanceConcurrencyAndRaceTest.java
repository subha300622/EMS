package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.exception.ActiveBreakExistsException;
import com.example.ems.attendance.exception.DuplicateCheckInException;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.AttendanceAdjustmentService;
import com.example.ems.attendance.service.AttendanceCorrectionService;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 2. 🚨 Concurrency & Race Condition Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceConcurrencyAndRaceTest {

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
    private com.example.ems.attendance.service.AttendancePolicyEvaluator policyEvaluator = new com.example.ems.attendance.service.AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    private Clock fixedClock;
    private Organization organization;
    private Employee employee;
    private AttendancePolicy policy;

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
        employee.setEmail("race.tester@alphacorp.com");
        employee.setFirstName("Race");
        employee.setLastName("Tester");
        employee.setOrganization(organization);
        employee.setStatus("ACTIVE");

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setOrganization(organization);
        policy.setName("Standard Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);
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

    // ── 1. Duplicate Check-In Race Condition ─────────────────────────────────

    @Test
    @DisplayName("Concurrency: Simultaneous check-in requests result in exactly ONE success and all collisions rejected")
    void testConcurrentDuplicateCheckIns_HandledSafely() throws Exception {
        mockSecurityContext(employee);
        when(policyService.getActivePolicy(ORG_ID)).thenReturn(policy);

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicBoolean firstCheckInDone = new AtomicBoolean(false);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenAnswer(i -> firstCheckInDone.get());

        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            if (firstCheckInDone.compareAndSet(false, true)) {
                Attendance att = i.getArgument(0);
                att.setId(777L);
                return att;
            } else {
                throw new DataIntegrityViolationException("duplicate key value violates unique constraint 'uk_attendance_employee_date'");
            }
        });

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    TenantContext.setCurrentTenant(ORG_ID);
                    mockSecurityContext(employee);

                    attendanceService.checkInCore();
                    successCount.incrementAndGet();
                } catch (DuplicateCheckInException e) {
                    duplicateCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly ONE check-in request must succeed");
        assertEquals(threadCount - 1, duplicateCount.get(), "All other simultaneous check-ins must be caught as DuplicateCheckInException");
        assertEquals(0, errorCount.get(), "No unexpected unhandled errors should occur");
    }

    // ── 2. Double Approval Race Condition ────────────────────────────────────

    @Test
    @DisplayName("Concurrency: Simultaneous approvals on same adjustment result in EXACTLY ONE state transition and correction")
    void testConcurrentDoubleApproval_OnlyOneSucceeds() throws Exception {
        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);

        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setId(501L);
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(organization);
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);
        adjustment.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        adjustment.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successApprovals = new AtomicInteger(0);
        AtomicInteger rejectedApprovals = new AtomicInteger(0);

        when(adjustmentRepository.findByIdAndOrganizationId(501L, ORG_ID)).thenAnswer(i -> {
            synchronized (adjustment) {
                AttendanceAdjustment copy = new AttendanceAdjustment();
                copy.setId(adjustment.getId());
                copy.setAttendance(adjustment.getAttendance());
                copy.setEmployee(adjustment.getEmployee());
                copy.setOrganization(adjustment.getOrganization());
                copy.setStatus(adjustment.getStatus());
                copy.setRequestedCheckInTime(adjustment.getRequestedCheckInTime());
                copy.setRequestedCheckOutTime(adjustment.getRequestedCheckOutTime());
                return Optional.of(copy);
            }
        });

        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment saved = i.getArgument(0);
            synchronized (adjustment) {
                if (adjustment.getStatus() == AttendanceAdjustmentStatus.APPROVED) {
                    throw new IllegalStateException("Adjustment request is not in PENDING state (current: APPROVED).");
                }
                adjustment.setStatus(saved.getStatus());
                return saved;
            }
        });

        for (int i = 0; i < threadCount; i++) {
            final int approverId = i + 1;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    TenantContext.setCurrentTenant(ORG_ID);

                    Authentication auth = mock(Authentication.class);
                    when(auth.getName()).thenReturn("approver" + approverId + "@alphacorp.com");
                    SecurityContext secCtx = mock(SecurityContext.class);
                    when(secCtx.getAuthentication()).thenReturn(auth);
                    SecurityContextHolder.setContext(secCtx);

                    AdjustmentApprovalRequest req = new AdjustmentApprovalRequest();
                    req.setRemarks("Approved by thread " + approverId);

                    adjustmentService.approveAdjustment(501L, req);
                    successApprovals.incrementAndGet();
                } catch (IllegalStateException e) {
                    rejectedApprovals.incrementAndGet();
                } catch (Exception e) {
                    // unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successApprovals.get(), "Exactly ONE approval must succeed");
        assertEquals(threadCount - 1, rejectedApprovals.get(), "All other concurrent approvals must be rejected with IllegalStateException");
        verify(correctionService, times(1)).applyCorrection(any(), any(), any(), any(), any(), eq("ADJUSTMENT"));
    }

    // ── 3. Concurrent Duplicate Break Start ──────────────────────────────────

    @Test
    @DisplayName("Concurrency: Simultaneous break starts result in exactly ONE active break created")
    void testConcurrentBreakStarts_SingleActiveBreakCreated() throws Exception {
        mockSecurityContext(employee);

        Attendance attendance = new Attendance();
        attendance.setId(101L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setStatus(AttendanceStatus.WORKING);

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successBreaks = new AtomicInteger(0);
        AtomicInteger collisionBreaks = new AtomicInteger(0);
        AtomicBoolean activeBreakOpen = new AtomicBoolean(false);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), any(LocalDate.class), eq(ORG_ID)))
                .thenReturn(Optional.of(attendance));

        when(attendanceBreakRepository.existsByAttendanceIdAndBreakEndTimeIsNull(101L))
                .thenAnswer(i -> activeBreakOpen.get());

        when(attendanceBreakRepository.save(any(AttendanceBreak.class))).thenAnswer(i -> {
            if (activeBreakOpen.compareAndSet(false, true)) {
                return i.getArgument(0);
            } else {
                throw new ActiveBreakExistsException("An active break is already in progress.");
            }
        });

        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    TenantContext.setCurrentTenant(ORG_ID);
                    mockSecurityContext(employee);

                    attendanceService.startBreakCore();
                    successBreaks.incrementAndGet();
                } catch (ActiveBreakExistsException | com.example.ems.attendance.exception.InvalidAttendanceStateException e) {
                    collisionBreaks.incrementAndGet();
                } catch (Exception e) {
                    // unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successBreaks.get(), "Exactly ONE break start must succeed");
        assertEquals(threadCount - 1, collisionBreaks.get(), "Concurrent attempts must encounter ActiveBreakExistsException");
    }
}
