package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class AttendancePolicyEvaluatorTest {

    private AttendancePolicyEvaluator evaluator;
    private AttendancePolicy policy;
    private ZoneId zone;

    @BeforeEach
    void setUp() {
        evaluator = new AttendancePolicyEvaluator();
        zone = ZoneId.of("Asia/Kolkata");

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setName("Standard General Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setLateGraceMinutes(15);
        policy.setEarlyExitGraceMinutes(15);
        policy.setMinimumWorkingMinutes(480);
        policy.setHalfDayThreshold(240);
        policy.setLateThreshold(15);
        policy.setEarlyCheckoutThreshold(15);
        policy.setMaximumBreakMinutes(60);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("Evaluate Check-In: On-time within grace period (09:10 AM)")
    void testEvaluateCheckIn_OnTimeWithinGrace() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkInTime = date.atTime(9, 10).atZone(zone).toInstant();

        AttendancePolicyEvaluator.CheckInEvaluation result = evaluator.evaluateCheckIn(checkInTime, policy, zone);

        assertNotNull(result);
        assertFalse(result.isLate());
        assertEquals(0, result.lateByMinutes());
    }

    @Test
    @DisplayName("Evaluate Check-In: Late arrival beyond grace period (09:30 AM)")
    void testEvaluateCheckIn_LateArrival() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkInTime = date.atTime(9, 30).atZone(zone).toInstant();

        AttendancePolicyEvaluator.CheckInEvaluation result = evaluator.evaluateCheckIn(checkInTime, policy, zone);

        assertNotNull(result);
        assertTrue(result.isLate());
        assertEquals(30, result.lateByMinutes());
    }

    @Test
    @DisplayName("Evaluate Check-In: Exactly at office start time (09:00 AM)")
    void testEvaluateCheckIn_ExactStart() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkInTime = date.atTime(9, 0).atZone(zone).toInstant();

        AttendancePolicyEvaluator.CheckInEvaluation result = evaluator.evaluateCheckIn(checkInTime, policy, zone);

        assertNotNull(result);
        assertFalse(result.isLate());
        assertEquals(0, result.lateByMinutes());
    }

    @Test
    @DisplayName("Evaluate Check-Out: Full day present without early checkout (18:00)")
    void testEvaluateCheckOut_FullDayOnTime() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkIn = date.atTime(9, 0).atZone(zone).toInstant();
        Instant checkOut = date.atTime(18, 0).atZone(zone).toInstant();

        AttendancePolicyEvaluator.CheckOutEvaluation result = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, zone);

        assertNotNull(result);
        assertFalse(result.isEarlyCheckout());
        assertEquals(0, result.earlyByMinutes());
        assertFalse(result.isHalfDay());
        assertEquals(540, result.totalWorkingMinutes());
    }

    @Test
    @DisplayName("Evaluate Check-Out: Early checkout before office end time (16:30)")
    void testEvaluateCheckOut_EarlyDeparture() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkIn = date.atTime(9, 0).atZone(zone).toInstant();
        Instant checkOut = date.atTime(16, 30).atZone(zone).toInstant(); // 90 mins early

        AttendancePolicyEvaluator.CheckOutEvaluation result = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, zone);

        assertNotNull(result);
        assertTrue(result.isEarlyCheckout());
        assertEquals(90, result.earlyByMinutes());
        assertFalse(result.isHalfDay()); // 450 mins >= 240 mins
        assertEquals(450, result.totalWorkingMinutes());
    }

    @Test
    @DisplayName("Evaluate Check-Out: Half-day triggered when total working minutes < halfDayThreshold (3.5 hours worked)")
    void testEvaluateCheckOut_HalfDayTriggered() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkIn = date.atTime(9, 0).atZone(zone).toInstant();
        Instant checkOut = date.atTime(12, 30).atZone(zone).toInstant(); // 210 mins < 240 threshold

        AttendancePolicyEvaluator.CheckOutEvaluation result = evaluator.evaluateCheckOut(checkIn, checkOut, 0, policy, zone);

        assertNotNull(result);
        assertTrue(result.isEarlyCheckout());
        assertTrue(result.isHalfDay());
        assertEquals(210, result.totalWorkingMinutes());
    }
}
