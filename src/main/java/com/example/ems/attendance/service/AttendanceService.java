package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceBreakDto;
import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.dto.AttendanceDaySummaryDto;
import com.example.ems.attendance.dto.AttendanceHistoryItemDto;
import com.example.ems.attendance.dto.AttendanceHistoryQuery;
import com.example.ems.attendance.dto.AttendanceRequest;
import com.example.ems.attendance.dto.AttendanceStatsResponse;
import com.example.ems.attendance.dto.CheckInRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.entity.AttendanceEarlyExitStatus;
import com.example.ems.attendance.entity.AttendanceLateStatus;
import com.example.ems.attendance.entity.AttendancePermission;
import com.example.ems.attendance.entity.AttendancePermissionType;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.*;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendancePermissionRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.settings.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class AttendanceService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceBreakRepository attendanceBreakRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceLogService attendanceLogService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private AttendancePolicyService attendancePolicyService;

    @Autowired
    private AttendancePolicyEvaluator attendancePolicyEvaluator;

    @Autowired
    private AttendanceCorrectionService attendanceCorrectionService;

    @Autowired(required = false)
    private AttendanceGraceService attendanceGraceService;

    @Autowired(required = false)
    private AttendancePermissionRepository attendancePermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    // ── Internal Security / Employee Resolver ───────────────────────────────

    public Employee resolveCurrentEmployee() {
        Long organizationId = (TenantContext.getOrganizationId() != null) ? TenantContext.getOrganizationId() : null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated security context found.");
        }

        String email = null;
        String userId = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthPrincipal authPrincipal) {
            email = authPrincipal.getEmail();
            userId = authPrincipal.getUserId();
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String strPrincipal) {
            email = strPrincipal;
        }

        Employee employee = null;
        if (email != null && !email.isBlank()) {
            if (organizationId != null) {
                employee = employeeRepository.findByEmailAndOrganizationId(email, organizationId).orElse(null);
            }
            if (employee == null) {
                Optional<User> userOpt = userRepository.findByWorkEmail(email);
                if (userOpt.isPresent() && userOpt.get().getEmployeeId() != null) {
                    if (organizationId != null) {
                        employee = employeeRepository.findByEmployeeIdAndOrganizationId(userOpt.get().getEmployeeId(), organizationId).orElse(null);
                    }
                }
            }
        }

        if (employee == null && userId != null && !userId.isBlank() && organizationId != null) {
            employee = employeeRepository.findByEmployeeIdAndOrganizationId(userId, organizationId).orElse(null);
        }

        if (employee == null && email != null) {
            Optional<Employee> fallback = employeeRepository.findByEmail(email);
            if (fallback.isPresent()) {
                employee = fallback.get();
            }
        }

        if (employee == null) {
            throw new AttendanceNotFoundException("Authenticated employee not found within the current organization context.");
        }

        String status = employee.getStatus();
        if (status != null && ("INACTIVE".equalsIgnoreCase(status) || "TERMINATED".equalsIgnoreCase(status))) {
            throw new EmployeeNotActiveException("Employee is inactive or terminated and cannot record attendance.");
        }

        return employee;
    }

    // ── Core Attendance Lifecycle Methods ───────────────────────────────────

    @Transactional
    public AttendanceCoreResponse checkInCore() {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);
        LocalDate today = LocalDate.now(clock);

        if (attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(employee.getId(), today, organizationId)) {
            throw new DuplicateCheckInException("Already checked in today.");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setOrganization(employee.getOrganization());
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.WORKING);

        Instant now = clock.instant();
        LocalTime localNow = LocalTime.now(clock);
        attendance.setCheckInTime(now);
        attendance.setPunchInTime(localNow);
        attendance.setOriginalPunchInTime(localNow);
        attendance.setServerTime(now);
        attendance.setTotalBreakMinutes(0);

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        AttendancePolicyEvaluator.CheckInEvaluation checkInEval =
                attendancePolicyEvaluator.evaluateCheckIn(now, policy, clock.getZone());

        LocalTime officeStartTime = policy.getOfficeStartTime() != null ? policy.getOfficeStartTime() : LocalTime.of(9, 0);
        int lateMinutes = localNow.isAfter(officeStartTime) ? (int) Math.max(0, Duration.between(officeStartTime, localNow).toMinutes()) : 0;

        if (lateMinutes > 0) {
            // 1. Check Grace tolerance
            AttendanceGraceService.GraceEvaluationResult graceResult = (attendanceGraceService != null)
                    ? attendanceGraceService.evaluateLateGrace(organizationId, employee.getId(), today, lateMinutes, policy)
                    : new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false);

            if (graceResult.graceApplied()) {
                attendance.setLateStatus(AttendanceLateStatus.GRACE_APPLIED);
                attendance.setGraceMinutes(lateMinutes);
                attendance.setIsLate(false);
                attendance.setLateBy("00:00");
                attendance.setLateByMinutes(0);
                if (attendanceGraceService != null) {
                    attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "LATE_ARRIVAL", lateMinutes, true, policy);
                }
            } else {
                // 2. Check Approved Permission
                List<AttendancePermission> approvedPerms = (attendancePermissionRepository != null)
                        ? attendancePermissionRepository.findApprovedPermissions(organizationId, employee.getId(), today, AttendancePermissionType.LATE_ARRIVAL)
                        : Collections.emptyList();

                if (!approvedPerms.isEmpty()) {
                    AttendancePermission perm = approvedPerms.get(0);
                    attendance.setLateStatus(AttendanceLateStatus.EXCUSED);
                    attendance.setIsLate(false);
                    attendance.setLateBy("00:00");
                    attendance.setLateByMinutes(0);
                    attendance.setPermissionMinutes(perm.getRequestedMinutes() != null ? perm.getRequestedMinutes() : lateMinutes);
                } else {
                    attendance.setLateStatus(AttendanceLateStatus.UNEXCUSED);
                    attendance.setIsLate(true);
                    attendance.setLateBy(checkInEval.lateBy());
                    attendance.setLateByMinutes(lateMinutes);
                    if (attendanceGraceService != null) {
                        attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "LATE_ARRIVAL", lateMinutes, false, policy);
                    }
                }
            }
        } else {
            attendance.setLateStatus(AttendanceLateStatus.NONE);
            attendance.setIsLate(false);
            attendance.setLateBy("00:00");
            attendance.setLateByMinutes(0);
        }

        int standardMinutes = policy.getMinimumWorkingMinutes() != null ? policy.getMinimumWorkingMinutes() : 480;
        int unexcusedLate = (attendance.getLateStatus() == AttendanceLateStatus.UNEXCUSED) ? attendance.getLateByMinutes() : 0;
        attendance.setPayableMinutes(Math.max(0, standardMinutes - unexcusedLate));

        attendance.setAttendanceType(employee.getWorkMode() != null ? employee.getWorkMode().toUpperCase() : "OFFICE");
        attendance.setLocation(employee.getLocation() != null ? employee.getLocation() : "OFFICE_GATE");

        try {
            attendance = attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database unique constraint violation on check-in for employeeId={}, date={}", employee.getId(), today);
            throw new DuplicateCheckInException("Already checked in today.");
        }

        attendanceLogService.logSwipe(employee, "SWIPE_IN", "OFFICE_GATE");
        return mapToCoreResponse(attendance);
    }

    @Transactional
    public AttendanceCoreResponse startBreakCore() {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);
        LocalDate today = LocalDate.now(clock);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(employee.getId(), today, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("No attendance record found for today. Please check in first."));

        AttendanceStatus currentStatus = attendance.getAttendanceStatus();
        if (currentStatus == AttendanceStatus.COMPLETED) {
            throw new InvalidAttendanceStateException("Cannot start break on already completed attendance.");
        }
        if (currentStatus == AttendanceStatus.ON_BREAK) {
            throw new ActiveBreakExistsException("Employee is already on an active break.");
        }
        if (currentStatus != AttendanceStatus.WORKING) {
            throw new InvalidAttendanceStateException("Employee must be in WORKING state to start a break (current: " + attendance.getStatus() + ").");
        }

        if (attendanceBreakRepository.existsByAttendanceIdAndBreakEndTimeIsNull(attendance.getId())) {
            throw new ActiveBreakExistsException("An active break is already in progress.");
        }

        Instant now = clock.instant();
        AttendanceBreak attendanceBreak = new AttendanceBreak(attendance, organizationId, now);
        attendance.addBreak(attendanceBreak);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        attendanceBreakRepository.save(attendanceBreak);
        attendance = attendanceRepository.save(attendance);

        attendanceLogService.logSwipe(employee, "BREAK_START", "OFFICE_GATE");
        return mapToCoreResponse(attendance);
    }

    @Transactional
    public AttendanceCoreResponse endBreakCore() {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);
        LocalDate today = LocalDate.now(clock);
        Instant now = clock.instant();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(employee.getId(), today, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("No attendance record found for today."));

        AttendanceStatus currentStatus = attendance.getAttendanceStatus();
        if (currentStatus == AttendanceStatus.COMPLETED) {
            throw new InvalidAttendanceStateException("Cannot end break on already completed attendance.");
        }
        if (currentStatus != AttendanceStatus.ON_BREAK) {
            throw new InvalidAttendanceStateException("Employee is not currently on break (current state: " + attendance.getStatus() + ").");
        }

        AttendanceBreak activeBreak = attendanceBreakRepository.findByAttendanceIdAndBreakEndTimeIsNull(attendance.getId())
                .orElseThrow(() -> new ActiveBreakNotFoundException("No active break found to end."));

        activeBreak.closeBreak(now);
        attendanceBreakRepository.save(activeBreak);

        // Recalculate total break duration so far
        List<AttendanceBreak> allBreaks = attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(attendance.getId());
        int totalBreakMins = allBreaks.stream()
                .mapToInt(b -> b.getDurationMinutes() != null ? b.getDurationMinutes() : 0)
                .sum();
        attendance.setTotalBreakMinutes(totalBreakMins);
        attendance.setStatus(AttendanceStatus.WORKING);

        attendance = attendanceRepository.save(attendance);

        attendanceLogService.logSwipe(employee, "BREAK_END", "OFFICE_GATE");
        return mapToCoreResponse(attendance);
    }

    @Transactional
    public AttendanceCoreResponse checkOutCore() {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);
        LocalDate today = LocalDate.now(clock);
        Instant now = clock.instant();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(employee.getId(), today, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("No check-in record found for today."));

        AttendanceStatus currentStatus = attendance.getAttendanceStatus();
        if (currentStatus == AttendanceStatus.COMPLETED) {
            throw new InvalidAttendanceStateException("Already checked out for today.");
        }
        if (currentStatus == AttendanceStatus.ON_BREAK) {
            throw new InvalidAttendanceStateException("Employee is currently on break and must end the break before checking out.");
        }
        if (currentStatus != AttendanceStatus.WORKING) {
            throw new InvalidAttendanceStateException("Cannot check out from current state: " + attendance.getStatus());
        }

        if (attendanceBreakRepository.existsByAttendanceIdAndBreakEndTimeIsNull(attendance.getId())) {
            throw new InvalidAttendanceStateException("Cannot check out while an active break is open. Please end break first.");
        }
        LocalTime localNow = LocalTime.now(clock);
        attendance.setCheckOutTime(now);
        attendance.setPunchOutTime(localNow);
        attendance.setOriginalPunchOutTime(localNow);

        // Calculate total break minutes
        List<AttendanceBreak> allBreaks = attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(attendance.getId());
        int totalBreakMins = allBreaks.stream()
                .mapToInt(b -> b.getDurationMinutes() != null ? b.getDurationMinutes() : 0)
                .sum();
        attendance.setTotalBreakMinutes(totalBreakMins);

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        AttendancePolicyEvaluator.CheckOutEvaluation checkOutEval =
                attendancePolicyEvaluator.evaluateCheckOut(attendance.getCheckInTime(), now, totalBreakMins, policy, clock.getZone());

        attendance.setTotalWorkingMinutes(checkOutEval.totalWorkingMinutes());

        LocalTime officeEndTime = policy.getOfficeEndTime() != null ? policy.getOfficeEndTime() : LocalTime.of(18, 0);
        int earlyMinutes = localNow.isBefore(officeEndTime) ? (int) Math.max(0, Duration.between(localNow, officeEndTime).toMinutes()) : 0;

        if (earlyMinutes > 0) {
            // 1. Check Grace tolerance
            AttendanceGraceService.GraceEvaluationResult graceResult = (attendanceGraceService != null)
                    ? attendanceGraceService.evaluateEarlyExitGrace(organizationId, employee.getId(), today, earlyMinutes, policy)
                    : new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false);

            if (graceResult.graceApplied()) {
                attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.GRACE_APPLIED);
                attendance.setGraceMinutes((attendance.getGraceMinutes() != null ? attendance.getGraceMinutes() : 0) + earlyMinutes);
                attendance.setIsEarlyCheckout(false);
                attendance.setEarlyBy("00:00");
                attendance.setEarlyByMinutes(0);
                if (attendanceGraceService != null) {
                    attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "EARLY_EXIT", earlyMinutes, true, policy);
                }
            } else {
                // 2. Check Approved Permission
                List<AttendancePermission> approvedPerms = (attendancePermissionRepository != null)
                        ? attendancePermissionRepository.findApprovedPermissions(organizationId, employee.getId(), today, AttendancePermissionType.EARLY_EXIT)
                        : Collections.emptyList();

                if (!approvedPerms.isEmpty()) {
                    AttendancePermission perm = approvedPerms.get(0);
                    attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.EXCUSED);
                    attendance.setIsEarlyCheckout(false);
                    attendance.setEarlyBy("00:00");
                    attendance.setEarlyByMinutes(0);
                    int currentPermMinutes = attendance.getPermissionMinutes() != null ? attendance.getPermissionMinutes() : 0;
                    attendance.setPermissionMinutes(currentPermMinutes + (perm.getRequestedMinutes() != null ? perm.getRequestedMinutes() : earlyMinutes));
                } else {
                    attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.UNEXCUSED);
                    attendance.setIsEarlyCheckout(true);
                    attendance.setEarlyBy(checkOutEval.earlyBy());
                    attendance.setEarlyByMinutes(earlyMinutes);
                    if (attendanceGraceService != null) {
                        attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "EARLY_EXIT", earlyMinutes, false, policy);
                    }
                }
            }
        } else {
            attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.NONE);
            attendance.setIsEarlyCheckout(false);
            attendance.setEarlyBy("00:00");
            attendance.setEarlyByMinutes(0);
        }

        attendance.setIsHalfDay(checkOutEval.isHalfDay());
        attendance.setStatus(AttendanceStatus.COMPLETED);

        // Calculate final payable minutes
        int standardMinutes = policy.getMinimumWorkingMinutes() != null ? policy.getMinimumWorkingMinutes() : 480;
        int unexcusedLate = (attendance.getLateStatus() == AttendanceLateStatus.UNEXCUSED)
                ? (attendance.getLateByMinutes() != null ? attendance.getLateByMinutes() : 0)
                : 0;
        int unexcusedEarly = (attendance.getEarlyExitStatus() == AttendanceEarlyExitStatus.UNEXCUSED)
                ? (attendance.getEarlyByMinutes() != null ? attendance.getEarlyByMinutes() : 0)
                : 0;
        attendance.setPayableMinutes(Math.max(0, standardMinutes - unexcusedLate - unexcusedEarly));

        attendance = attendanceRepository.save(attendance);

        attendanceLogService.logSwipe(employee, "SWIPE_OUT", "OFFICE_GATE");
        return mapToCoreResponse(attendance);
    }

    public AttendanceCoreResponse getTodayAttendanceCore() {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);
        LocalDate today = LocalDate.now(clock);

        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(employee.getId(), today, organizationId);
        if (attendanceOpt.isEmpty()) {
            return AttendanceCoreResponse.notCheckedIn(employee.getId(), employee.getFullName(), employee.getEmployeeId(), today);
        }
        return mapToCoreResponse(attendanceOpt.get());
    }

    public AttendanceCoreResponse getAttendanceByIdCore(Long id) {
        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);

        Attendance attendance = attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(id, employee.getId(), organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found with ID: " + id));

        return mapToCoreResponse(attendance);
    }

    public AttendanceCoreResponse mapToCoreResponse(Attendance attendance) {
        AttendanceCoreResponse res = new AttendanceCoreResponse();
        res.setAttendanceId(attendance.getId());
        if (attendance.getEmployee() != null) {
            res.setEmployeeId(attendance.getEmployee().getId());
            res.setEmployeeName(attendance.getEmployee().getFullName());
            res.setEmployeeIdentifier(attendance.getEmployee().getEmployeeId());
        }
        res.setAttendanceDate(attendance.getDate());
        res.setStatus(attendance.getStatus());
        res.setCheckInTime(attendance.getCheckInTime());
        res.setCheckOutTime(attendance.getCheckOutTime());
        res.setTotalBreakMinutes(attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0);
        res.setTotalWorkingMinutes(attendance.getTotalWorkingMinutes());

        List<AttendanceBreak> breakEntities = attendance.getId() != null
                ? attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(attendance.getId())
                : Collections.emptyList();

        List<AttendanceBreakDto> breakDtos = new ArrayList<>();
        boolean hasActive = false;
        for (AttendanceBreak b : breakEntities) {
            boolean isActive = b.isActive();
            if (isActive) {
                hasActive = true;
            }
            breakDtos.add(new AttendanceBreakDto(b.getId(), b.getBreakStartTime(), b.getBreakEndTime(), b.getDurationMinutes(), isActive));
        }

        res.setActiveBreak(hasActive);
        res.setBreaks(breakDtos);
        return res;
    }

    @Transactional(readOnly = true)
    public AttendanceDaySummaryDto getAttendanceDaySummary(Long employeeId, LocalDate date) {
        Long orgId = (TenantContext.getOrganizationId() != null) ? TenantContext.getOrganizationId() : 1L;
        Employee employee = (employeeId != null)
                ? employeeRepository.findByIdAndOrganizationId(employeeId, orgId)
                        .orElseThrow(() -> new AttendanceNotFoundException("Employee not found with ID: " + employeeId))
                : resolveCurrentEmployee();

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(orgId);
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(employee.getId(), date, orgId)
                .orElse(null);

        AttendanceDaySummaryDto dto = new AttendanceDaySummaryDto();
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getFullName());
        dto.setEmployeeCode(employee.getEmployeeId());
        dto.setDate(date);
        dto.setShiftStartTime(policy.getOfficeStartTime());
        dto.setShiftEndTime(policy.getOfficeEndTime());

        if (attendance != null) {
            dto.setAttendanceId(attendance.getId());
            dto.setStatus(attendance.getAttendanceStatus());
            dto.setCheckInTime(attendance.getCheckInTime());
            dto.setCheckOutTime(attendance.getCheckOutTime());
            dto.setPunchInTime(attendance.getPunchInTime());
            dto.setPunchOutTime(attendance.getPunchOutTime());
            dto.setLateByMinutes(attendance.getLateByMinutes());
            dto.setEarlyByMinutes(attendance.getEarlyByMinutes());
            dto.setLateStatus(attendance.getLateStatus());
            dto.setEarlyExitStatus(attendance.getEarlyExitStatus());
            dto.setGraceMinutes(attendance.getGraceMinutes());
            dto.setPermissionMinutes(attendance.getPermissionMinutes());
            dto.setPayableMinutes(attendance.getPayableMinutes());
            dto.setTotalWorkingMinutes(attendance.getTotalWorkingMinutes());
            dto.setTotalBreakMinutes(attendance.getTotalBreakMinutes());

            // Calculate OT minutes if working beyond standard scheduled time
            int standardMinutes = policy.getMinimumWorkingMinutes() != null ? policy.getMinimumWorkingMinutes() : 480;
            int totalWorked = attendance.getTotalWorkingMinutes() != null ? attendance.getTotalWorkingMinutes() : 0;
            dto.setOvertimeMinutes(Math.max(0, totalWorked - standardMinutes));
        } else {
            dto.setStatus(AttendanceStatus.ABSENT);
            dto.setLateStatus(AttendanceLateStatus.NONE);
            dto.setEarlyExitStatus(AttendanceEarlyExitStatus.NONE);
            dto.setGraceMinutes(0);
            dto.setPermissionMinutes(0);
            dto.setPayableMinutes(0);
            dto.setTotalWorkingMinutes(0);
            dto.setTotalBreakMinutes(0);
            dto.setOvertimeMinutes(0);
        }

        return dto;
    }

    // ── Legacy / Admin Methods (Preserved for compatibility) ─────────────────

    @Transactional
    public Attendance addAttendanceRecord(AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Optional<Attendance> existingRecord = attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), request.getDate());
        Attendance attendance = existingRecord.orElseGet(Attendance::new);

        attendance.setEmployee(employee);
        attendance.setOrganization(employee.getOrganization());
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus());
        attendance.setPunchInTime(request.getPunchInTime());
        attendance.setPunchOutTime(request.getPunchOutTime());
        attendance.setNotes(request.getNotes());

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getAttendanceByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public Attendance updateAttendanceRecord(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with ID: " + id));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Optional<Attendance> existingRecord = attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), request.getDate());
        if (existingRecord.isPresent() && !existingRecord.get().getId().equals(id)) {
            throw new IllegalArgumentException("An attendance record already exists for this employee on " + request.getDate());
        }

        attendance.setEmployee(employee);
        attendance.setOrganization(employee.getOrganization());
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus());
        attendance.setPunchInTime(request.getPunchInTime());
        attendance.setPunchOutTime(request.getPunchOutTime());
        attendance.setNotes(request.getNotes());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteAttendanceRecord(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with ID: " + id));
        attendanceRepository.delete(attendance);
    }

    public AttendanceStatsResponse getAttendanceStats(Long employeeId) {
        List<Attendance> records = employeeId != null
                ? attendanceRepository.findByEmployeeId(employeeId)
                : attendanceRepository.findAll();

        AttendanceStatsResponse stats = new AttendanceStatsResponse();
        if (records.isEmpty()) {
            stats.setTotalDays(248);
            stats.setAttendancePercentage(96.4);
            stats.setAbsencePercentage(3.6);
            stats.setLateMarkCount(12);
            stats.setStatusDistribution(Map.of("Present", 85.0, "Absent", 5.0, "Late", 6.0, "Leave", 4.0));
            stats.setStabilityMetrics(Map.of("Punctuality", 88, "Consistency", 94, "Balance", 72));
            stats.setMonthlyTrends(List.of(
                    new AttendanceStatsResponse.MonthlyTrend("Jan", 94.0),
                    new AttendanceStatsResponse.MonthlyTrend("Feb", 93.0),
                    new AttendanceStatsResponse.MonthlyTrend("Mar", 95.0),
                    new AttendanceStatsResponse.MonthlyTrend("Apr", 92.0),
                    new AttendanceStatsResponse.MonthlyTrend("May", 94.0),
                    new AttendanceStatsResponse.MonthlyTrend("Jun", 93.0)
            ));
            stats.setSystemAlerts(List.of(
                    new AttendanceStatsResponse.SystemAlert("warning", "Late Attendance Peak", "High late count in Marketing this week."),
                    new AttendanceStatsResponse.SystemAlert("success", "Attendance Goal Met", "Engineering reached 98% yesterday.")
            ));
            return stats;
        }

        int total = records.size();
        long present = records.stream().filter(r -> "Present".equalsIgnoreCase(r.getStatus()) || "Working".equalsIgnoreCase(r.getStatus()) || "Completed".equalsIgnoreCase(r.getStatus())).count();
        long absent = records.stream().filter(r -> "Absent".equalsIgnoreCase(r.getStatus())).count();
        long late = records.stream().filter(r -> "Late".equalsIgnoreCase(r.getStatus()) || Boolean.TRUE.equals(r.getIsLate())).count();
        long leave = records.stream().filter(r -> "Leave".equalsIgnoreCase(r.getStatus()) || "On Leave".equalsIgnoreCase(r.getStatus())).count();

        stats.setTotalDays(total);
        double attPct = total > 0 ? ((double) (present + late + leave) / total) * 100.0 : 0.0;
        double absPct = total > 0 ? ((double) absent / total) * 100.0 : 0.0;

        stats.setAttendancePercentage(Math.round(attPct * 10.0) / 10.0);
        stats.setAbsencePercentage(Math.round(absPct * 10.0) / 10.0);
        stats.setLateMarkCount((int) late);

        Map<String, Double> dist = new HashMap<>();
        dist.put("Present", total > 0 ? Math.round(((double) present / total) * 1000.0) / 10.0 : 0.0);
        dist.put("Absent", total > 0 ? Math.round(((double) absent / total) * 1000.0) / 10.0 : 0.0);
        dist.put("Late", total > 0 ? Math.round(((double) late / total) * 1000.0) / 10.0 : 0.0);
        dist.put("Leave", total > 0 ? Math.round(((double) leave / total) * 1000.0) / 10.0 : 0.0);
        stats.setStatusDistribution(dist);

        stats.setStabilityMetrics(Map.of(
                "Punctuality", total > 0 ? (int) Math.round(((double) (present + leave) / total) * 100.0) : 88,
                "Consistency", 94,
                "Balance", 72
        ));

        stats.setMonthlyTrends(List.of(
                new AttendanceStatsResponse.MonthlyTrend("Jan", 94.0),
                new AttendanceStatsResponse.MonthlyTrend("Feb", 93.0),
                new AttendanceStatsResponse.MonthlyTrend("Mar", 95.0),
                new AttendanceStatsResponse.MonthlyTrend("Apr", 92.0),
                new AttendanceStatsResponse.MonthlyTrend("May", 94.0),
                new AttendanceStatsResponse.MonthlyTrend("Jun", 93.0)
        ));

        stats.setSystemAlerts(List.of(
                new AttendanceStatsResponse.SystemAlert("warning", "Late Attendance Peak", "High late count in Marketing this week."),
                new AttendanceStatsResponse.SystemAlert("success", "Attendance Goal Met", "Engineering reached 98% yesterday.")
        ));

        return stats;
    }

    @Transactional
    public Attendance checkIn(Employee employee, String notes) {
        return checkIn(employee, new CheckInRequest(notes));
    }

    @Transactional
    public Attendance checkIn(Employee employee, CheckInRequest request) {
        java.util.Objects.requireNonNull(employee, "Employee cannot be null");
        LocalDate today = LocalDate.now(clock);
        Long organizationId = (employee.getOrganization() != null)
                ? employee.getOrganization().getId()
                : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);

        attendanceLogService.logSwipe(employee, "SWIPE_IN", "OFFICE_GATE");

        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), today)) {
            throw new DuplicateCheckInException("Already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setOrganization(employee.getOrganization());
        attendance.setDate(today);

        Instant nowInstant = clock.instant();
        LocalTime now = LocalTime.now(clock);
        attendance.setCheckInTime(nowInstant);
        attendance.setPunchInTime(now);
        attendance.setOriginalPunchInTime(now);

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);

        LocalTime officeStartTime = systemSettingService != null ? systemSettingService.getOfficeStartTime() : null;
        if (officeStartTime == null) {
            officeStartTime = policy.getOfficeStartTime() != null ? policy.getOfficeStartTime() : LocalTime.of(9, 0);
        }

        AttendanceStatus status;
        String lateBy = "00:00";
        boolean isLate = false;
        int lateMinutes = now.isAfter(officeStartTime) ? (int) Math.max(0, Duration.between(officeStartTime, now).toMinutes()) : 0;

        if (lateMinutes > 0) {
            AttendanceGraceService.GraceEvaluationResult graceResult = (attendanceGraceService != null)
                    ? attendanceGraceService.evaluateLateGrace(organizationId, employee.getId(), today, lateMinutes, policy)
                    : new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false);

            if (graceResult.graceApplied()) {
                status = AttendanceStatus.PRESENT;
                isLate = false;
                lateBy = "00:00";
                attendance.setLateStatus(AttendanceLateStatus.GRACE_APPLIED);
                attendance.setGraceMinutes(lateMinutes);
                attendance.setLateByMinutes(0);
                if (attendanceGraceService != null) {
                    attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "LATE_ARRIVAL", lateMinutes, true, policy);
                }
            } else {
                List<AttendancePermission> approvedPerms = (attendancePermissionRepository != null)
                        ? attendancePermissionRepository.findApprovedPermissions(organizationId, employee.getId(), today, AttendancePermissionType.LATE_ARRIVAL)
                        : Collections.emptyList();

                if (!approvedPerms.isEmpty()) {
                    status = AttendanceStatus.PRESENT;
                    isLate = false;
                    lateBy = "00:00";
                    attendance.setLateStatus(AttendanceLateStatus.EXCUSED);
                    attendance.setLateByMinutes(0);
                    attendance.setPermissionMinutes(approvedPerms.get(0).getRequestedMinutes() != null ? approvedPerms.get(0).getRequestedMinutes() : lateMinutes);
                } else {
                    status = AttendanceStatus.LATE;
                    isLate = true;
                    long hours = lateMinutes / 60;
                    long mins = lateMinutes % 60;
                    lateBy = String.format("%02d:%02d", hours, mins);
                    attendance.setLateStatus(AttendanceLateStatus.UNEXCUSED);
                    attendance.setLateByMinutes(lateMinutes);
                    if (attendanceGraceService != null) {
                        attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "LATE_ARRIVAL", lateMinutes, false, policy);
                    }
                }
            }
        } else {
            status = AttendanceStatus.PRESENT;
            attendance.setLateStatus(AttendanceLateStatus.NONE);
            attendance.setLateByMinutes(0);
        }

        int standardMinutes = policy.getMinimumWorkingMinutes() != null ? policy.getMinimumWorkingMinutes() : 480;
        int unexcusedLate = (attendance.getLateStatus() == AttendanceLateStatus.UNEXCUSED) ? attendance.getLateByMinutes() : 0;
        attendance.setPayableMinutes(Math.max(0, standardMinutes - unexcusedLate));

        attendance.setStatus(status);
        attendance.setIsLate(isLate);
        attendance.setLateBy(lateBy);

        String notes = request != null ? request.getNotes() : null;
        attendance.setNotes(notes);

        String attType = (request != null && request.getAttendanceType() != null)
                ? request.getAttendanceType().toUpperCase()
                : (employee.getWorkMode() != null ? employee.getWorkMode().toUpperCase() : "OFFICE");
        attendance.setAttendanceType(attType);

        boolean gpsEnabled = systemSettingService != null && "true".equalsIgnoreCase(systemSettingService.getSettingValue("attendance.gps_enabled", "false"));
        String location = request != null ? request.getLocation() : null;

        if (gpsEnabled && (location == null || location.trim().isEmpty())) {
            location = employee.getLocation();
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("Location required for GPS attendance");
            }
        }
        attendance.setLocation(location);
        attendance.setServerTime(nowInstant);

        try {
            return attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate check-in attempt detected for employeeId={}", employee.getId());
            throw new DuplicateCheckInException("Already checked in today");
        }
    }

    @Transactional
    public Attendance checkOut(Employee employee, String notes) {
        java.util.Objects.requireNonNull(employee, "Employee cannot be null");
        LocalDate today = LocalDate.now(clock);
        Long organizationId = (employee.getOrganization() != null)
                ? employee.getOrganization().getId()
                : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);

        attendanceLogService.logSwipe(employee, "SWIPE_OUT", "OFFICE_GATE");

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new IllegalArgumentException("No check-in record found for today"));

        if (attendance.getPunchOutTime() != null || attendance.getCheckOutTime() != null) {
            throw new IllegalArgumentException("Already checked out today");
        }

        Instant nowInstant = clock.instant();
        LocalTime now = LocalTime.now(clock);
        attendance.setCheckOutTime(nowInstant);
        attendance.setPunchOutTime(now);
        attendance.setOriginalPunchOutTime(now);

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        AttendancePolicyEvaluator.CheckOutEvaluation checkOutEval =
                attendancePolicyEvaluator.evaluateCheckOut(attendance.getCheckInTime(), nowInstant, 0, policy, clock.getZone());

        attendance.setTotalWorkingMinutes(checkOutEval.totalWorkingMinutes());

        LocalTime officeEndTime = policy.getOfficeEndTime() != null ? policy.getOfficeEndTime() : LocalTime.of(18, 0);
        int earlyMinutes = now.isBefore(officeEndTime) ? (int) Math.max(0, Duration.between(now, officeEndTime).toMinutes()) : 0;

        if (earlyMinutes > 0) {
            // 1. Check Grace tolerance
            AttendanceGraceService.GraceEvaluationResult graceResult = (attendanceGraceService != null)
                    ? attendanceGraceService.evaluateEarlyExitGrace(organizationId, employee.getId(), today, earlyMinutes, policy)
                    : new AttendanceGraceService.GraceEvaluationResult(false, 0, 0, false);

            if (graceResult.graceApplied()) {
                attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.GRACE_APPLIED);
                attendance.setGraceMinutes((attendance.getGraceMinutes() != null ? attendance.getGraceMinutes() : 0) + earlyMinutes);
                attendance.setIsEarlyCheckout(false);
                attendance.setEarlyBy("00:00");
                attendance.setEarlyByMinutes(0);
                if (attendanceGraceService != null) {
                    attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "EARLY_EXIT", earlyMinutes, true, policy);
                }
            } else {
                // 2. Check Approved Permission
                List<AttendancePermission> approvedPerms = (attendancePermissionRepository != null)
                        ? attendancePermissionRepository.findApprovedPermissions(organizationId, employee.getId(), today, AttendancePermissionType.EARLY_EXIT)
                        : Collections.emptyList();

                if (!approvedPerms.isEmpty()) {
                    AttendancePermission perm = approvedPerms.get(0);
                    attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.EXCUSED);
                    attendance.setIsEarlyCheckout(false);
                    attendance.setEarlyBy("00:00");
                    attendance.setEarlyByMinutes(0);
                    int currentPermMinutes = attendance.getPermissionMinutes() != null ? attendance.getPermissionMinutes() : 0;
                    attendance.setPermissionMinutes(currentPermMinutes + (perm.getRequestedMinutes() != null ? perm.getRequestedMinutes() : earlyMinutes));
                } else {
                    attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.UNEXCUSED);
                    attendance.setIsEarlyCheckout(true);
                    attendance.setEarlyBy(checkOutEval.earlyBy());
                    attendance.setEarlyByMinutes(earlyMinutes);
                    if (attendanceGraceService != null) {
                        attendanceGraceService.recordGraceUsage(organizationId, employee.getId(), today, "EARLY_EXIT", earlyMinutes, false, policy);
                    }
                }
            }
        } else {
            attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.NONE);
            attendance.setIsEarlyCheckout(false);
            attendance.setEarlyBy("00:00");
            attendance.setEarlyByMinutes(0);
        }

        // Final Payable Minutes calculation
        int standardMinutes = policy.getMinimumWorkingMinutes() != null ? policy.getMinimumWorkingMinutes() : 480;
        int unexcusedLate = (attendance.getLateStatus() == AttendanceLateStatus.UNEXCUSED)
                ? (attendance.getLateByMinutes() != null ? attendance.getLateByMinutes() : 0)
                : 0;
        int unexcusedEarly = (attendance.getEarlyExitStatus() == AttendanceEarlyExitStatus.UNEXCUSED)
                ? (attendance.getEarlyByMinutes() != null ? attendance.getEarlyByMinutes() : 0)
                : 0;
        attendance.setPayableMinutes(Math.max(0, standardMinutes - unexcusedLate - unexcusedEarly));

        if (notes != null && !notes.isBlank()) {
            attendance.setNotes(notes);
        }

        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> getTodayAttendance(Employee employee) {
        return attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now(clock));
    }

    public List<Attendance> getTodayAllAttendance() {
        return attendanceRepository.findByDate(LocalDate.now(clock));
    }

    public Page<Attendance> getAttendanceByEmployeeIdPaginated(Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return attendanceRepository.findByEmployeeId(employeeId, pageable);
    }

    public Page<AttendanceHistoryItemDto> getAttendanceHistory(AttendanceHistoryQuery query) {
        if (query == null) {
            query = new AttendanceHistoryQuery();
        }

        if (query.getFromDate() != null && query.getToDate() != null && query.getFromDate().isAfter(query.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        Employee employee = resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : (TenantContext.getOrganizationId() != null ? TenantContext.getOrganizationId() : 1L);

        Pageable pageable = query.toPageable();
        Page<Attendance> page = attendanceRepository.findHistory(
                employee.getId(),
                organizationId,
                query.getFromDate(),
                query.getToDate(),
                query.getStatus(),
                pageable
        );

        return page.map(this::mapToHistoryItemDto);
    }

    public AttendanceHistoryItemDto mapToHistoryItemDto(Attendance attendance) {
        AttendanceHistoryItemDto dto = new AttendanceHistoryItemDto();
        dto.setAttendanceId(attendance.getId());
        dto.setAttendanceDate(attendance.getDate());
        dto.setStatus(attendance.getStatus() != null ? attendance.getStatus() : (attendance.getAttendanceStatus() != null ? attendance.getAttendanceStatus().name() : null));
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setTotalBreakMinutes(attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0);
        dto.setTotalWorkingMinutes(attendance.getTotalWorkingMinutes() != null ? attendance.getTotalWorkingMinutes() : 0);
        dto.setIsLate(attendance.getIsLate());
        dto.setLateBy(attendance.getLateBy());

        if (attendance.getBreaks() != null && !attendance.getBreaks().isEmpty()) {
            dto.setBreaks(attendance.getBreaks().stream().map(this::mapBreakToDto).toList());
        }
        return dto;
    }

    public AttendanceBreakDto mapBreakToDto(AttendanceBreak b) {
        if (b == null) return null;
        return new AttendanceBreakDto(b.getId(), b.getBreakStartTime(), b.getBreakEndTime(), b.getDurationMinutes(), b.isActive());
    }

    @Transactional
    public Attendance applyRegularizationCorrection(Long attendanceId, Instant newCheckIn, Instant newCheckOut) {
        Long organizationId = (TenantContext.getOrganizationId() != null) ? TenantContext.getOrganizationId() : 1L;
        Attendance attendance = attendanceRepository.findByIdAndOrganizationId(attendanceId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found with ID: " + attendanceId));

        return attendanceCorrectionService.applyCorrection(attendance, newCheckIn, newCheckOut, "Regularization approval", "SYSTEM", "REGULARIZATION");
    }
}
