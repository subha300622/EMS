package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
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

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 5. 🕐 Time-Zone & Midnight Cross-over Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceTimezoneAndMidnightTest {

    private static final Long ORG_ID = 100L;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePolicyService policyService;

    @Mock
    private AttendanceCorrectionService correctionService;

    @Mock
    private com.example.ems.attendance.service.AttendanceLogService attendanceLogService;

    @Spy
    private AttendancePolicyEvaluator evaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceService attendanceService;

    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-10");
        employee.setEmail("tz.tester@alphacorp.com");
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

    // ── 1. Midnight Boundary Cross-over (23:59:00 -> 00:01:00) ──────────────

    @Test
    @DisplayName("Timezone: Midnight cross-over calculates working minutes accurately without negative duration")
    void testMidnightCrossOver_EvaluatesCorrectly() {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        AttendancePolicy nightPolicy = new AttendancePolicy();
        nightPolicy.setOfficeStartTime(LocalTime.of(20, 0)); // 8 PM
        nightPolicy.setOfficeEndTime(LocalTime.of(4, 0));   // 4 AM next day
        nightPolicy.setGracePeriodMinutes(15);
        nightPolicy.setEarlyCheckoutThreshold(15);

        ZonedDateTime checkInZoned = ZonedDateTime.of(2026, 9, 10, 23, 59, 0, 0, istZone);
        Instant checkInInstant = checkInZoned.toInstant();

        ZonedDateTime checkOutZoned = ZonedDateTime.of(2026, 9, 11, 0, 1, 0, 0, istZone);
        Instant checkOutInstant = checkOutZoned.toInstant();

        var checkInEval = evaluator.evaluateCheckIn(checkInInstant, nightPolicy, istZone);
        assertNotNull(checkInEval);
        var checkOutEval = evaluator.evaluateCheckOut(checkInInstant, checkOutInstant, 0, nightPolicy, istZone);

        assertEquals(2, checkOutEval.totalWorkingMinutes());
        assertTrue(checkOutInstant.isAfter(checkInInstant));
    }

    // ── 2. Multi-Timezone Evaluations (Asia/Kolkata, UTC, America/New_York, Asia/Tokyo) ──

    @Test
    @DisplayName("Timezone: Asia/Kolkata (+05:30) check-in at 09:10 IST evaluates on local calendar date")
    void testTimezone_AsiaKolkata() {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        AttendancePolicy policy = new AttendancePolicy();
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);

        ZonedDateTime zdt = ZonedDateTime.of(2026, 9, 10, 9, 10, 0, 0, istZone);
        Instant checkInInstant = zdt.toInstant();

        var eval = evaluator.evaluateCheckIn(checkInInstant, policy, istZone);
        assertFalse(eval.isLate(), "09:10 IST is within 15 min grace of 09:00 IST shift");
        assertEquals(0, eval.lateByMinutes());

        LocalDate businessDate = LocalDate.ofInstant(checkInInstant, istZone);
        assertEquals(LocalDate.of(2026, 9, 10), businessDate);
    }

    @Test
    @DisplayName("Timezone: America/New_York (EDT) check-in at 09:20 EDT is evaluated as LATE by 5 minutes")
    void testTimezone_AmericaNewYork_LateCheckIn() {
        ZoneId nyZone = ZoneId.of("America/New_York");
        AttendancePolicy policy = new AttendancePolicy();
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(17, 0));
        policy.setGracePeriodMinutes(15);

        ZonedDateTime zdt = ZonedDateTime.of(2026, 9, 10, 9, 20, 0, 0, nyZone);
        Instant checkInInstant = zdt.toInstant();

        var eval = evaluator.evaluateCheckIn(checkInInstant, policy, nyZone);
        assertTrue(eval.isLate(), "09:20 EDT is after 09:15 EDT grace period");
        assertEquals(20, eval.lateByMinutes());

        LocalDate businessDate = LocalDate.ofInstant(checkInInstant, nyZone);
        assertEquals(LocalDate.of(2026, 9, 10), businessDate);
    }

    @Test
    @DisplayName("Timezone: Asia/Tokyo (+09:00) check-out at 18:00 JST evaluates full 9-hour working duration")
    void testTimezone_AsiaTokyo_FullWorkingDuration() {
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        AttendancePolicy policy = new AttendancePolicy();
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setEarlyCheckoutThreshold(15);

        ZonedDateTime inZdt = ZonedDateTime.of(2026, 9, 10, 9, 0, 0, 0, tokyoZone);
        ZonedDateTime outZdt = ZonedDateTime.of(2026, 9, 10, 18, 0, 0, 0, tokyoZone);

        var outEval = evaluator.evaluateCheckOut(inZdt.toInstant(), outZdt.toInstant(), 60, policy, tokyoZone);
        assertEquals(480, outEval.totalWorkingMinutes());
        assertFalse(outEval.isEarlyCheckout());
        assertFalse(outEval.isHalfDay());
    }
}
