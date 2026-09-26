package com.example.ems.attendance.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.CreateRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationApprovalRequest;
import com.example.ems.attendance.dto.RegularizationProcessRequest;
import com.example.ems.attendance.dto.RegularizationResponseDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class AttendanceRegularizationService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRegularizationService.class);

    @Autowired
    private AttendanceRegularizationRepository attendanceRegularizationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired(required = false)
    private ApprovalFacade approvalFacade;

    @Autowired
    private RoleService roleService;

    // ── Phase 3: Regularization Creation & Double-Scoped Queries ───────────

    @Transactional
    public RegularizationResponseDto createRegularization(CreateRegularizationRequest request) {
        if (request == null || request.getAttendanceId() == null) {
            throw new IllegalArgumentException("attendanceId is mandatory.");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is mandatory.");
        }
        if (request.getRequestedCheckInTime() == null && request.getRequestedCheckOutTime() == null) {
            throw new IllegalArgumentException("At least one requested timestamp (check-in or check-out) must be provided.");
        }

        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();

        // Double-scoped verification: attendance must belong to current employee and tenant
        Attendance attendance = attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(request.getAttendanceId(), employee.getId(), organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found with ID: " + request.getAttendanceId()));

        // Concurrency check: Ensure no active PENDING request exists
        if (attendanceRegularizationRepository.existsByAttendanceIdAndStatus(attendance.getId(), AttendanceRegularizationStatus.PENDING)) {
            throw new IllegalStateException("An active pending regularization request already exists for this attendance record.");
        }

        // Effective pair validation
        Instant effectiveCheckIn = request.getRequestedCheckInTime() != null ? request.getRequestedCheckInTime() : attendance.getCheckInTime();
        Instant effectiveCheckOut = request.getRequestedCheckOutTime() != null ? request.getRequestedCheckOutTime() : attendance.getCheckOutTime();

        if (effectiveCheckIn != null && effectiveCheckOut != null && !effectiveCheckOut.isAfter(effectiveCheckIn)) {
            throw new IllegalArgumentException("Requested check-out time must be after check-in time.");
        }

        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setAttendance(attendance);
        reg.setEmployee(employee);
        reg.setOrganization(employee.getOrganization());
        reg.setDate(attendance.getDate());
        reg.setRequestedCheckInTime(request.getRequestedCheckInTime());
        reg.setRequestedCheckOutTime(request.getRequestedCheckOutTime());
        reg.setReason(request.getReason());
        reg.setStatus(AttendanceRegularizationStatus.PENDING);

        if (request.getRequestedCheckInTime() != null) {
            reg.setProposedPunchInTime(LocalTime.ofInstant(request.getRequestedCheckInTime(), java.time.ZoneOffset.UTC));
        }
        if (request.getRequestedCheckOutTime() != null) {
            reg.setProposedPunchOutTime(LocalTime.ofInstant(request.getRequestedCheckOutTime(), java.time.ZoneOffset.UTC));
        }

        try {
            reg = attendanceRegularizationRepository.save(reg);
        } catch (DataIntegrityViolationException e) {
            log.warn("Unique constraint violation for pending regularization on attendanceId={}", attendance.getId());
            throw new IllegalStateException("An active pending regularization request already exists for this attendance record.");
        }

        // Trigger central approval workflow
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext();
                context.setModule("ATTENDANCE_REGULARIZATION");
                context.setResourceId(String.valueOf(reg.getId()));
                context.setEmployeeId(String.valueOf(employee.getId()));
                context.setDepartmentId(1L);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    reg.setWorkflowInstanceId(String.valueOf(instance.getId()));
                    reg = attendanceRegularizationRepository.save(reg);
                }
            } catch (Exception e) {
                log.warn("Failed to spawn approval workflow instance for regularization {}: {}", reg.getId(), e.getMessage());
            }
        }

        return mapToResponseDto(reg);
    }

    public Page<RegularizationResponseDto> getMyRegularizations(AttendanceRegularizationStatus status, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();

        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AttendanceRegularization> regPage = attendanceRegularizationRepository.findMyRegularizations(
                employee.getId(),
                organizationId,
                status,
                fromDate,
                toDate,
                pageable
        );

        return regPage.map(this::mapToResponseDto);
    }

    public RegularizationResponseDto getRegularizationById(Long id) {
        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();

        AttendanceRegularization reg = attendanceRegularizationRepository.findByIdAndEmployeeIdAndOrganizationId(id, employee.getId(), organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Regularization request not found with ID: " + id));

        return mapToResponseDto(reg);
    }

    // ── Phase 3: Cancellation ───────────────────────────────────────────────

    @Transactional
    public RegularizationResponseDto cancelRegularization(Long id, String reason) {
        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();

        AttendanceRegularization reg = attendanceRegularizationRepository.findByIdAndEmployeeIdAndOrganizationId(id, employee.getId(), organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Regularization request not found with ID: " + id));

        if (reg.getStatus() != AttendanceRegularizationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING regularization requests can be cancelled (current: " + reg.getStatus() + ").");
        }

        reg.setStatus(AttendanceRegularizationStatus.CANCELLED);
        reg.setManagerNotes(reason != null ? reason : "Cancelled by employee");
        reg = attendanceRegularizationRepository.save(reg);

        if (approvalFacade != null && reg.getWorkflowInstanceId() != null) {
            try {
                approvalFacade.cancel(
                        WorkflowType.ATTENDANCE_REGULARIZATION,
                        "ATTENDANCE_REGULARIZATION",
                        String.valueOf(reg.getId()),
                        reason != null ? reason : "Cancelled by employee"
                );
            } catch (Exception e) {
                log.warn("Failed to cancel central workflow for regularization {}: {}", reg.getId(), e.getMessage());
            }
        }

        return mapToResponseDto(reg);
    }

    // ── Phase 4: Direct Approver Operations ─────────────────────────────────

    @Transactional
    public RegularizationResponseDto approveRegularization(Long id, RegularizationApprovalRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceRegularization reg = attendanceRegularizationRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Regularization request not found with ID: " + id));

        if (reg.getStatus() != AttendanceRegularizationStatus.PENDING) {
            throw new IllegalStateException("Regularization request is not in PENDING state (current: " + reg.getStatus() + ").");
        }

        String approverName = resolveCurrentUserName();
        reg.setStatus(AttendanceRegularizationStatus.APPROVED);
        reg.setApprovedBy(approverName);
        reg.setApprovedAt(Instant.now());
        if (request != null && request.getRemarks() != null) {
            reg.setManagerNotes(request.getRemarks());
        }
        reg = attendanceRegularizationRepository.save(reg);

        // Apply centralized domain correction on attendance
        if (reg.getAttendance() != null) {
            attendanceService.applyRegularizationCorrection(
                    reg.getAttendance().getId(),
                    reg.getRequestedCheckInTime(),
                    reg.getRequestedCheckOutTime()
            );
        }

        return mapToResponseDto(reg);
    }

    @Transactional
    public RegularizationResponseDto rejectRegularization(Long id, RegularizationApprovalRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceRegularization reg = attendanceRegularizationRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Regularization request not found with ID: " + id));

        if (reg.getStatus() != AttendanceRegularizationStatus.PENDING) {
            throw new IllegalStateException("Regularization request is not in PENDING state (current: " + reg.getStatus() + ").");
        }

        reg.setStatus(AttendanceRegularizationStatus.REJECTED);
        String remarks = (request != null && request.getRemarks() != null) ? request.getRemarks() : "Rejected by approver";
        reg.setRejectionReason(remarks);
        reg.setManagerNotes(remarks);
        reg = attendanceRegularizationRepository.save(reg);

        return mapToResponseDto(reg);
    }

    public RegularizationResponseDto mapToResponseDto(AttendanceRegularization reg) {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(reg.getId());
        if (reg.getAttendance() != null) {
            dto.setAttendanceId(reg.getAttendance().getId());
            dto.setOriginalCheckInTime(reg.getAttendance().getCheckInTime());
            dto.setOriginalCheckOutTime(reg.getAttendance().getCheckOutTime());
        }
        if (reg.getEmployee() != null) {
            dto.setEmployeeId(reg.getEmployee().getId());
            dto.setEmployeeName(reg.getEmployee().getFullName());
            dto.setEmployeeCode(reg.getEmployee().getEmployeeId());
        }
        dto.setAttendanceDate(reg.getDate());
        dto.setRequestedCheckInTime(reg.getRequestedCheckInTime());
        dto.setRequestedCheckOutTime(reg.getRequestedCheckOutTime());
        dto.setStatus(reg.getStatus());
        dto.setReason(reg.getReason());
        dto.setManagerNotes(reg.getManagerNotes());
        dto.setRejectionReason(reg.getRejectionReason());
        dto.setWorkflowInstanceId(reg.getWorkflowInstanceId());
        dto.setApprovedBy(reg.getApprovedBy());
        dto.setApprovedAt(reg.getApprovedAt());
        dto.setCreatedAt(reg.getCreatedAt());
        dto.setUpdatedAt(reg.getUpdatedAt());
        return dto;
    }

    private String resolveCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "APPROVER";
    }

    // ── Legacy Methods (Preserved for compatibility) ─────────────────────────

    public List<AttendanceRegularization> getRegularizationsForUser(User currentUser, String status) {
        String email = currentUser.getWorkEmail();
        boolean hasAdminPerm = roleService.hasPermission(email, PermissionRegistry.ATTENDANCE_READ)
                || roleService.hasPermission(email, PermissionRegistry.ATTENDANCE_MANAGE);

        boolean hasSelfPerm = roleService.hasPermission(email, PermissionRegistry.ATTENDANCE_SELF_READ)
                || roleService.hasPermission(email, PermissionRegistry.EMPLOYEE_ATTENDANCE_READ);

        if (!hasAdminPerm && !hasSelfPerm) {
            throw new SecurityException("Access Denied: Requires attendance permissions.");
        }

        if (hasAdminPerm) {
            if (status == null || status.isBlank()) {
                return attendanceRegularizationRepository.findAll();
            }
            return attendanceRegularizationRepository.findByStatus(status);
        } else {
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("Employee profile not found for authenticated user."));
            if (status == null || status.isBlank()) {
                return attendanceRegularizationRepository.findByEmployeeId(employee.getId());
            }
            return attendanceRegularizationRepository.findByEmployeeIdAndStatus(employee.getId(), status);
        }
    }

    @Transactional
    public AttendanceRegularization submitRegularization(Long employeeId, LocalDate date, LocalTime proposedPunchIn, LocalTime proposedPunchOut, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        AttendanceRegularization regularization = new AttendanceRegularization();
        regularization.setEmployee(employee);
        regularization.setDate(date);
        regularization.setProposedPunchInTime(proposedPunchIn);
        regularization.setProposedPunchOutTime(proposedPunchOut);
        regularization.setReason(reason);
        regularization.setStatus(AttendanceRegularizationStatus.PENDING);

        return attendanceRegularizationRepository.save(regularization);
    }

    public List<AttendanceRegularization> getRegularizations(String status) {
        if (status == null || status.isBlank()) {
            return attendanceRegularizationRepository.findAll();
        }
        return attendanceRegularizationRepository.findByStatus(status);
    }

    @Transactional
    public AttendanceRegularization approveRegularization(Long id, RegularizationProcessRequest request) {
        AttendanceRegularization reg = attendanceRegularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found with ID: " + id));
        if (reg.getStatus() != AttendanceRegularizationStatus.PENDING) {
            throw new IllegalArgumentException("Regularization request is already processed: " + reg.getStatus());
        }

        reg.setStatus(AttendanceRegularizationStatus.APPROVED);
        if (request != null && request.getManagerNotes() != null) {
            reg.setManagerNotes(request.getManagerNotes());
        }
        attendanceRegularizationRepository.save(reg);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(reg.getEmployee().getId(), reg.getDate())
                .orElseGet(() -> {
                    Attendance newRecord = new Attendance();
                    newRecord.setEmployee(reg.getEmployee());
                    newRecord.setDate(reg.getDate());
                    return newRecord;
                });

        LocalTime approvedPunchIn = (request != null && request.getCorrectedPunchInTime() != null)
                ? request.getCorrectedPunchInTime()
                : reg.getProposedPunchInTime();

        LocalTime approvedPunchOut = (request != null && request.getCorrectedPunchOutTime() != null)
                ? request.getCorrectedPunchOutTime()
                : reg.getProposedPunchOutTime();

        if (approvedPunchIn != null) {
            attendance.setPunchInTime(approvedPunchIn);
        }
        if (approvedPunchOut != null) {
            attendance.setPunchOutTime(approvedPunchOut);
        }

        if (approvedPunchIn != null && approvedPunchIn.isAfter(LocalTime.of(9, 30))) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("PRESENT");
        }

        String notes = "Regularized: " + reg.getReason();
        if (request != null && request.getManagerNotes() != null && !request.getManagerNotes().isBlank()) {
            notes += " (Manager Notes: " + request.getManagerNotes() + ")";
        }
        attendance.setNotes(notes);

        attendanceRepository.save(attendance);
        return reg;
    }

    @Transactional
    public AttendanceRegularization rejectRegularization(Long id, RegularizationProcessRequest request) {
        AttendanceRegularization reg = attendanceRegularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found with ID: " + id));
        if (reg.getStatus() != AttendanceRegularizationStatus.PENDING) {
            throw new IllegalArgumentException("Regularization request is already processed: " + reg.getStatus());
        }

        reg.setStatus(AttendanceRegularizationStatus.REJECTED);
        if (request != null && request.getManagerNotes() != null) {
            reg.setManagerNotes(request.getManagerNotes());
        }
        return attendanceRegularizationRepository.save(reg);
    }
}
