package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendanceGraceUsage;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.GracePeriodType;
import com.example.ems.attendance.repository.AttendanceGraceUsageRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Service
public class AttendanceGraceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceGraceService.class);

    @Autowired
    private AttendanceGraceUsageRepository graceUsageRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public record PeriodBounds(LocalDate startDate, LocalDate endDate) {}

    public PeriodBounds calculatePeriodBounds(LocalDate date, GracePeriodType periodType) {
        if (date == null) {
            date = LocalDate.now();
        }
        if (periodType == null) {
            periodType = GracePeriodType.MONTHLY;
        }

        return switch (periodType) {
            case DAILY -> new PeriodBounds(date, date);
            case WEEKLY -> new PeriodBounds(
                    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            );
            case YEARLY -> new PeriodBounds(
                    date.with(TemporalAdjusters.firstDayOfYear()),
                    date.with(TemporalAdjusters.lastDayOfYear())
            );
            case MONTHLY -> new PeriodBounds(
                    date.with(TemporalAdjusters.firstDayOfMonth()),
                    date.with(TemporalAdjusters.lastDayOfMonth())
            );
        };
    }

    public record GraceEvaluationResult(
            boolean graceApplied,
            int graceMinutes,
            long occurrencesUsedInPeriod,
            boolean limitExceeded
    ) {}

    @Transactional(readOnly = true)
    public GraceEvaluationResult evaluateLateGrace(Long orgId, Long employeeId, LocalDate date, int lateMinutes, AttendancePolicy policy) {
        if (policy == null || !Boolean.TRUE.equals(policy.getAllowLateGrace()) || lateMinutes <= 0) {
            return new GraceEvaluationResult(false, 0, 0, false);
        }

        int maxGraceMinutes = policy.getLateGraceMinutes() != null ? policy.getLateGraceMinutes() : 10;
        if (lateMinutes > maxGraceMinutes) {
            // Arrived later than allowed grace tolerance
            return new GraceEvaluationResult(false, 0, 0, false);
        }

        PeriodBounds bounds = calculatePeriodBounds(date, policy.getGracePeriodType());
        long currentUsages = graceUsageRepository.countGraceUsagesInPeriod(
                orgId, employeeId, "LATE_ARRIVAL", bounds.startDate(), bounds.endDate()
        );

        int maxOccurrences = policy.getGraceOccurrencesPerPeriod() != null ? policy.getGraceOccurrencesPerPeriod() : 3;
        if (currentUsages >= maxOccurrences) {
            log.info("Employee {} has exhausted late grace occurrences ({}/{}) for period {} to {}",
                    employeeId, currentUsages, maxOccurrences, bounds.startDate(), bounds.endDate());
            return new GraceEvaluationResult(false, 0, currentUsages, true);
        }

        return new GraceEvaluationResult(true, lateMinutes, currentUsages + 1, false);
    }

    @Transactional(readOnly = true)
    public GraceEvaluationResult evaluateEarlyExitGrace(Long orgId, Long employeeId, LocalDate date, int earlyMinutes, AttendancePolicy policy) {
        if (policy == null || !Boolean.TRUE.equals(policy.getAllowEarlyExitGrace()) || earlyMinutes <= 0) {
            return new GraceEvaluationResult(false, 0, 0, false);
        }

        int maxGraceMinutes = policy.getEarlyExitGraceMinutes() != null ? policy.getEarlyExitGraceMinutes() : 10;
        if (earlyMinutes > maxGraceMinutes) {
            return new GraceEvaluationResult(false, 0, 0, false);
        }

        PeriodBounds bounds = calculatePeriodBounds(date, policy.getGracePeriodType());
        long currentUsages = graceUsageRepository.countGraceUsagesInPeriod(
                orgId, employeeId, "EARLY_EXIT", bounds.startDate(), bounds.endDate()
        );

        int maxOccurrences = policy.getGraceOccurrencesPerPeriod() != null ? policy.getGraceOccurrencesPerPeriod() : 3;
        if (currentUsages >= maxOccurrences) {
            log.info("Employee {} has exhausted early exit grace occurrences ({}/{}) for period {} to {}",
                    employeeId, currentUsages, maxOccurrences, bounds.startDate(), bounds.endDate());
            return new GraceEvaluationResult(false, 0, currentUsages, true);
        }

        return new GraceEvaluationResult(true, earlyMinutes, currentUsages + 1, false);
    }

    @Transactional
    public AttendanceGraceUsage recordGraceUsage(Long orgId, Long employeeId, LocalDate date, String graceType, int minutesUsed, boolean withinGrace, AttendancePolicy policy) {
        Optional<AttendanceGraceUsage> existing = graceUsageRepository.findByOrgEmpDateAndType(orgId, employeeId, date, graceType);
        AttendanceGraceUsage usage;
        if (existing.isPresent()) {
            usage = existing.get();
            usage.setGraceMinutesUsed(minutesUsed);
            usage.setWithinGrace(withinGrace);
            if (policy != null) {
                usage.setPolicy(policy);
            }
        } else {
            Organization org = organizationRepository.findById(orgId).orElse(null);
            Employee emp = employeeRepository.findById(employeeId).orElse(null);
            if (org == null || emp == null) {
                log.warn("Cannot record grace usage: org or employee not found for orgId={}, empId={}", orgId, employeeId);
                return null;
            }
            usage = new AttendanceGraceUsage(org, emp, date, graceType, minutesUsed, withinGrace, policy);
        }

        return graceUsageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public long getGraceUsagesCount(Long orgId, Long employeeId, String graceType, LocalDate date, GracePeriodType periodType) {
        PeriodBounds bounds = calculatePeriodBounds(date, periodType);
        return graceUsageRepository.countGraceUsagesInPeriod(orgId, employeeId, graceType, bounds.startDate(), bounds.endDate());
    }
}
