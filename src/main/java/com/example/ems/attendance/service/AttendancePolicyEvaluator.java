package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendancePolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class AttendancePolicyEvaluator {

    public record CheckInEvaluation(
            boolean isLate,
            int lateByMinutes,
            String lateBy,
            LocalTime localCheckIn,
            LocalTime officeStartTime,
            int gracePeriodMinutes
    ) {}

    public record CheckOutEvaluation(
            int totalWorkingMinutes,
            boolean isEarlyCheckout,
            int earlyByMinutes,
            String earlyBy,
            boolean isHalfDay,
            LocalTime localCheckOut,
            LocalTime officeEndTime
    ) {}

    public CheckInEvaluation evaluateCheckIn(Instant checkInTime, AttendancePolicy policy, ZoneId zoneId) {
        if (checkInTime == null || policy == null) {
            return new CheckInEvaluation(false, 0, "00:00", LocalTime.MIDNIGHT, LocalTime.of(9, 0), 15);
        }

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.systemDefault();
        LocalTime localCheckIn = LocalTime.ofInstant(checkInTime, effectiveZone);
        LocalTime officeStartTime = policy.getOfficeStartTime() != null ? policy.getOfficeStartTime() : LocalTime.of(9, 0);
        int gracePeriodMinutes = policy.getGracePeriodMinutes() != null ? policy.getGracePeriodMinutes() : (policy.getLateGraceMinutes() != null ? policy.getLateGraceMinutes() : 15);

        int lateByMinutes = 0;
        String lateBy = "00:00";
        boolean isLate = false;

        LocalTime graceDeadline = officeStartTime.plusMinutes(gracePeriodMinutes);
        if (localCheckIn.isAfter(graceDeadline)) {
            isLate = true;
            Duration lateDur = Duration.between(officeStartTime, localCheckIn);
            lateByMinutes = (int) Math.max(0, lateDur.toMinutes());
            lateBy = String.format("%02d:%02d", lateDur.toHours(), lateDur.toMinutesPart());
        }

        return new CheckInEvaluation(isLate, lateByMinutes, lateBy, localCheckIn, officeStartTime, gracePeriodMinutes);
    }

    public CheckOutEvaluation evaluateCheckOut(Instant checkInTime, Instant checkOutTime, int totalBreakMinutes, AttendancePolicy policy, ZoneId zoneId) {
        if (checkOutTime == null || policy == null) {
            return new CheckOutEvaluation(0, false, 0, "00:00", false, LocalTime.MIDNIGHT, LocalTime.of(18, 0));
        }

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.systemDefault();
        LocalTime localCheckOut = LocalTime.ofInstant(checkOutTime, effectiveZone);
        LocalTime officeEndTime = policy.getOfficeEndTime() != null ? policy.getOfficeEndTime() : LocalTime.of(18, 0);
        int earlyGrace = policy.getEarlyCheckoutThreshold() != null ? policy.getEarlyCheckoutThreshold() : (policy.getEarlyExitGraceMinutes() != null ? policy.getEarlyExitGraceMinutes() : 15);
        int halfDayThreshold = policy.getHalfDayThreshold() != null ? policy.getHalfDayThreshold() : 240;

        int totalWorkingMinutes = 0;
        if (checkInTime != null && checkOutTime.isAfter(checkInTime)) {
            long totalGrossDuration = Duration.between(checkInTime, checkOutTime).toMinutes();
            totalWorkingMinutes = (int) Math.max(0, totalGrossDuration - totalBreakMinutes);
        }

        boolean isHalfDay = totalWorkingMinutes < halfDayThreshold;

        int earlyByMinutes = 0;
        String earlyBy = "00:00";
        boolean isEarlyCheckout = false;

        LocalTime earlyThresholdTime = officeEndTime.minusMinutes(earlyGrace);
        if (localCheckOut.isBefore(earlyThresholdTime)) {
            isEarlyCheckout = true;
            Duration earlyDur = Duration.between(localCheckOut, officeEndTime);
            earlyByMinutes = (int) Math.max(0, earlyDur.toMinutes());
            earlyBy = String.format("%02d:%02d", earlyDur.toHours(), earlyDur.toMinutesPart());
        }

        return new CheckOutEvaluation(totalWorkingMinutes, isEarlyCheckout, earlyByMinutes, earlyBy, isHalfDay, localCheckOut, officeEndTime);
    }
}
