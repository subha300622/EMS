package com.example.ems.attendance.adversarial;

import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.attendance.service.AttendancePolicyEvaluator;
import com.example.ems.attendance.service.AttendancePolicyService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Level 2 Adversarial Testing: 4. 🧪 Boundary Testing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceBoundaryAndEvaluatorTest {

    private static final Long ORG_ID = 100L;
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Spy
    private AttendancePolicyEvaluator evaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendancePolicyService policyService;

    private AttendancePolicy policy;
    private Organization organization;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Alpha Corp");

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setOrganization(organization);
        policy.setName("Standard Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setEarlyCheckoutThreshold(15);
        policy.setHalfDayThreshold(240);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── 1. Policy Timing Boundaries ─────────────────────────────────────────

    @Test
    @DisplayName("Boundary: Shift start == Shift end (0 duration) is rejected")
    void testPolicyBoundary_StartEqualsEnd_ThrowsException() {
        CreateAttendancePolicyRequest req = new CreateAttendancePolicyRequest();
        req.setName("Zero Duration Policy");
        req.setOfficeStartTime(LocalTime.of(9, 0));
        req.setOfficeEndTime(LocalTime.of(9, 0));

        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
    }

    @Test
    @DisplayName("Boundary: Shift end before Shift start is rejected")
    void testPolicyBoundary_EndBeforeStart_ThrowsException() {
        CreateAttendancePolicyRequest req = new CreateAttendancePolicyRequest();
        req.setName("Inverted Policy");
        req.setOfficeStartTime(LocalTime.of(18, 0));
        req.setOfficeEndTime(LocalTime.of(9, 0));

        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
    }

    @Test
    @DisplayName("Boundary: Negative Grace Period is rejected")
    void testPolicyBoundary_NegativeGracePeriod_ThrowsException() {
        CreateAttendancePolicyRequest req = new CreateAttendancePolicyRequest();
        req.setName("Negative Grace Policy");
        req.setOfficeStartTime(LocalTime.of(9, 0));
        req.setOfficeEndTime(LocalTime.of(18, 0));
        req.setGracePeriodMinutes(-15);

        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(req));
    }

    // ── 2. Late Boundary Testing (09:00 + 15 min grace) ──────────────────────

    @Test
    @DisplayName("Boundary: Check-in at 09:14:59 (1 second before grace end) is NOT LATE")
    void testLateBoundary_09_14_59_NotLate() {
        Instant checkIn = Instant.parse("2026-09-10T09:14:59Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertFalse(eval.isLate());
        assertEquals(0, eval.lateByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-in at exact grace limit 09:15:00 is NOT LATE")
    void testLateBoundary_09_15_00_NotLate() {
        Instant checkIn = Instant.parse("2026-09-10T09:15:00Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertFalse(eval.isLate());
        assertEquals(0, eval.lateByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-in at 09:15:01 (1 second past grace) is LATE by 15 minutes (from shift start)")
    void testLateBoundary_09_15_01_Late() {
        Instant checkIn = Instant.parse("2026-09-10T09:15:01Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertTrue(eval.isLate());
        assertEquals(15, eval.lateByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-in at 09:16:00 is LATE by 16 minutes (from shift start)")
    void testLateBoundary_09_16_00_Late() {
        Instant checkIn = Instant.parse("2026-09-10T09:16:00Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertTrue(eval.isLate());
        assertEquals(16, eval.lateByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-in at 10:00:00 is LATE by 60 minutes")
    void testLateBoundary_10_00_00_Late() {
        Instant checkIn = Instant.parse("2026-09-10T10:00:00Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertTrue(eval.isLate());
        assertEquals(60, eval.lateByMinutes());
    }

    // ── 3. Early Checkout Boundary Testing (18:00 end, 15 min threshold) ───

    @Test
    @DisplayName("Boundary: Check-out at 17:44:59 (before 17:45 threshold) is EARLY CHECKOUT by 15 minutes")
    void testEarlyBoundary_17_44_59_Early() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T17:44:59Z");
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertTrue(eval.isEarlyCheckout());
        assertEquals(15, eval.earlyByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-out at exact threshold 17:45:00 is NOT EARLY")
    void testEarlyBoundary_17_45_00_NotEarly() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T17:45:00Z");
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertFalse(eval.isEarlyCheckout());
        assertEquals(0, eval.earlyByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-out at 17:46:00 is NOT EARLY")
    void testEarlyBoundary_17_46_00_NotEarly() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T17:46:00Z");
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertFalse(eval.isEarlyCheckout());
        assertEquals(0, eval.earlyByMinutes());
    }

    @Test
    @DisplayName("Boundary: Check-out at exact shift end 18:00:00 is NOT EARLY")
    void testEarlyBoundary_18_00_00_NotEarly() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T18:00:00Z");
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertFalse(eval.isEarlyCheckout());
        assertEquals(0, eval.earlyByMinutes());
    }

    // ── 4. Half-Day Working Duration Boundaries ──────────────────────────────

    @Test
    @DisplayName("Boundary: Working 239 minutes (1 min under 240 threshold) qualifies as HALF_DAY")
    void testHalfDayBoundary_239Minutes_IsHalfDay() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T12:59:00Z"); // 239 min
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertEquals(239, eval.totalWorkingMinutes());
        assertTrue(eval.isHalfDay());
    }

    @Test
    @DisplayName("Boundary: Working exactly 240 minutes qualifies as FULL SHIFT (not half day)")
    void testHalfDayBoundary_240Minutes_NotHalfDay() {
        Instant checkIn = Instant.parse("2026-09-10T09:00:00Z");
        Instant checkOut = Instant.parse("2026-09-10T13:00:00Z"); // 240 min
        var eval = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, UTC);

        assertEquals(240, eval.totalWorkingMinutes());
        assertFalse(eval.isHalfDay());
    }

    // ── 5. Grace Period Extreme Values (0 min grace and 60 min grace) ────────

    @Test
    @DisplayName("Boundary: Zero minute grace period makes 09:00:01 LATE")
    void testZeroGracePeriod_AnySecondLate_IsLate() {
        policy.setGracePeriodMinutes(0);
        Instant checkIn = Instant.parse("2026-09-10T09:00:01Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertTrue(eval.isLate());
        assertEquals(0, eval.lateByMinutes());
    }

    @Test
    @DisplayName("Boundary: 60 minute grace period allows check-in up to 10:00:00 without being late")
    void testExtendedGracePeriod_60Minutes_NotLate() {
        policy.setGracePeriodMinutes(60);
        Instant checkIn = Instant.parse("2026-09-10T09:59:59Z");
        var eval = evaluator.evaluateCheckIn(checkIn, policy, UTC);

        assertFalse(eval.isLate());
        assertEquals(0, eval.lateByMinutes());
    }
}
