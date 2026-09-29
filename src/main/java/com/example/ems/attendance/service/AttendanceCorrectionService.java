package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class AttendanceCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceCorrectionService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendancePolicyService attendancePolicyService;

    @Autowired
    private AttendancePolicyEvaluator attendancePolicyEvaluator;

    @Autowired(required = false)
    private Clock clock = Clock.systemDefaultZone();

    /**
     * Applies centralized attendance timestamps correction, recalculates durations,
     * late/early status against the tenant's active policy, and persists the record.
     */
    @Transactional
    public Attendance applyCorrection(Attendance attendance, Instant newCheckIn, Instant newCheckOut, String reason, String actorName, String source) {
        if (attendance == null) {
            throw new IllegalArgumentException("attendance cannot be null.");
        }

        Long organizationId = attendance.getOrganization() != null ? attendance.getOrganization().getId() : null;
        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        ZoneId zoneId = clock != null ? clock.getZone() : ZoneId.systemDefault();

        if (newCheckIn != null) {
            attendance.setCheckInTime(newCheckIn);
            LocalTime localCheckIn = LocalTime.ofInstant(newCheckIn, zoneId);
            attendance.setPunchInTime(localCheckIn);
        }

        if (newCheckOut != null) {
            attendance.setCheckOutTime(newCheckOut);
            LocalTime localCheckOut = LocalTime.ofInstant(newCheckOut, zoneId);
            attendance.setPunchOutTime(localCheckOut);
        }

        // Re-evaluate Check-In against Policy
        if (attendance.getCheckInTime() != null) {
            AttendancePolicyEvaluator.CheckInEvaluation checkInEval =
                    attendancePolicyEvaluator.evaluateCheckIn(attendance.getCheckInTime(), policy, zoneId);
            attendance.setIsLate(checkInEval.isLate());
            attendance.setLateBy(checkInEval.lateBy());
            attendance.setLateByMinutes(checkInEval.lateByMinutes());
        }

        // Re-evaluate Check-Out against Policy
        int totalBreakMinutes = attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0;
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            AttendancePolicyEvaluator.CheckOutEvaluation checkOutEval =
                    attendancePolicyEvaluator.evaluateCheckOut(attendance.getCheckInTime(), attendance.getCheckOutTime(), totalBreakMinutes, policy, zoneId);

            attendance.setTotalWorkingMinutes(checkOutEval.totalWorkingMinutes());
            attendance.setIsEarlyCheckout(checkOutEval.isEarlyCheckout());
            attendance.setEarlyBy(checkOutEval.earlyBy());
            attendance.setEarlyByMinutes(checkOutEval.earlyByMinutes());
            attendance.setIsHalfDay(checkOutEval.isHalfDay());
            attendance.setStatus(AttendanceStatus.COMPLETED);
        }

        attendance = attendanceRepository.save(attendance);
        log.info("Attendance ID={} corrected by actor='{}' via source='{}'. Reason='{}'",
                attendance.getId(), actorName, source, reason);

        return attendance;
    }
}
