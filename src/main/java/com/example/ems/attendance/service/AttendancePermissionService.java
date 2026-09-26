package com.example.ems.attendance.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.permission.AttendancePermissionCreateRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionRejectRequest;
import com.example.ems.attendance.dto.permission.AttendancePermissionResponse;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendancePermissionRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class AttendancePermissionService {

    private static final Logger log = LoggerFactory.getLogger(AttendancePermissionService.class);

    @Autowired
    private AttendancePermissionRepository permissionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendancePolicyService policyService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AttendanceGraceService graceService;

    @Autowired(required = false)
    private ApprovalFacade approvalFacade;

    @Transactional
    public AttendancePermissionResponse createPermission(AttendancePermissionCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null.");
        }
        if (request.getAttendanceDate() == null) {
            throw new IllegalArgumentException("attendanceDate is mandatory.");
        }
        if (request.getPermissionType() == null) {
            throw new IllegalArgumentException("permissionType is mandatory.");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is mandatory.");
        }

        Long orgId = TenantContext.requireOrganizationId();
        Employee employee = resolveTargetEmployee(request.getEmployeeId(), orgId);

        // Fetch active policy to validate quota limits
        AttendancePolicy policy = policyService.getActivePolicy(orgId);
        validateQuotaLimits(orgId, employee.getId(), request, policy);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new AttendanceNotFoundException("Organization not found with ID: " + orgId));

        AttendancePermission permission = new AttendancePermission();
        permission.setOrganization(organization);
        permission.setEmployee(employee);
        permission.setAttendanceDate(request.getAttendanceDate());
        permission.setPermissionType(request.getPermissionType());
        permission.setRequestedMinutes(request.getRequestedMinutes() != null ? request.getRequestedMinutes() : 0);
        permission.setExpectedTime(request.getExpectedTime());
        permission.setActualTime(request.getActualTime());
        permission.setReason(request.getReason());
        permission.setStatus(AttendancePermissionStatus.PENDING);

        permission = permissionRepository.save(permission);

        // Start approval workflow through central ApprovalFacade if present
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext();
                context.setModule("ATTENDANCE_PERMISSION");
                context.setResourceId(String.valueOf(permission.getId()));
                context.setEmployeeId(String.valueOf(employee.getId()));
                context.setDepartmentId(employee.getDepartment() != null ? 1L : null);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    permission.setWorkflowInstanceId(String.valueOf(instance.getId()));
                    permission = permissionRepository.save(permission);
                }
            } catch (Exception e) {
                log.warn("Could not start workflow instance for permission {}: {}", permission.getId(), e.getMessage());
            }
        }

        return AttendancePermissionResponse.fromEntity(permission);
    }

    private void validateQuotaLimits(Long orgId, Long employeeId, AttendancePermissionCreateRequest request, AttendancePolicy policy) {
        if (policy == null) return;

        LocalDate date = request.getAttendanceDate();
        AttendanceGraceService.PeriodBounds bounds = graceService.calculatePeriodBounds(date, policy.getGracePeriodType());

        int reqMinutes = request.getRequestedMinutes() != null ? request.getRequestedMinutes() : 0;

        // 1. Max monthly occurrences
        int maxOccurrences = policy.getMaxMonthlyPermissions() != null ? policy.getMaxMonthlyPermissions() : 4;
        long currentOccurrences = permissionRepository.countPermissionsInPeriod(orgId, employeeId, bounds.startDate(), bounds.endDate());
        if (currentOccurrences >= maxOccurrences) {
            throw new IllegalStateException("Monthly attendance permission occurrence limit reached (" + currentOccurrences + "/" + maxOccurrences + ").");
        }

        // 2. Max daily permission minutes
        int maxDailyMinutes = policy.getMaxDailyPermissionMinutes() != null ? policy.getMaxDailyPermissionMinutes() : 120;
        int dailyUsedMinutes = permissionRepository.sumMinutesForDate(orgId, employeeId, date);
        if (dailyUsedMinutes + reqMinutes > maxDailyMinutes) {
            throw new IllegalStateException("Requested permission minutes (" + reqMinutes + ") exceeds remaining daily limit (" + (maxDailyMinutes - dailyUsedMinutes) + " mins remaining).");
        }

        // 3. Max monthly permission minutes
        int maxMonthlyMinutes = policy.getMaxMonthlyPermissionMinutes() != null ? policy.getMaxMonthlyPermissionMinutes() : 480;
        int monthlyUsedMinutes = permissionRepository.sumMinutesInPeriod(orgId, employeeId, bounds.startDate(), bounds.endDate());
        if (monthlyUsedMinutes + reqMinutes > maxMonthlyMinutes) {
            throw new IllegalStateException("Requested permission minutes exceeds monthly limit (" + (maxMonthlyMinutes - monthlyUsedMinutes) + " mins remaining).");
        }
    }

    @Transactional(readOnly = true)
    public AttendancePermissionResponse getPermissionById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        AttendancePermission permission = permissionRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance permission not found with ID: " + id));
        return AttendancePermissionResponse.fromEntity(permission);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePermissionResponse> getMyPermissions(Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Employee employee = resolveCurrentEmployee();
        return permissionRepository.findByOrganizationIdAndEmployeeIdOrderByAttendanceDateDesc(orgId, employee.getId(), pageable)
                .map(AttendancePermissionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePermissionResponse> getPendingPermissions(Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        return permissionRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(orgId, AttendancePermissionStatus.PENDING, pageable)
                .map(AttendancePermissionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePermissionResponse> getPermissions(Long employeeId, AttendancePermissionStatus status, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Long orgId = TenantContext.requireOrganizationId();
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return permissionRepository.findPermissionsFiltered(orgId, employeeId, status, fromDate, toDate, pageable)
                .map(AttendancePermissionResponse::fromEntity);
    }

    @Transactional
    public AttendancePermissionResponse approvePermission(Long id, String comments) {
        Long orgId = TenantContext.requireOrganizationId();
        AttendancePermission permission = permissionRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance permission not found with ID: " + id));

        if (permission.getStatus() != AttendancePermissionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING permissions can be approved (current: " + permission.getStatus() + ").");
        }

        String approver = resolveCurrentUserName();
        permission.setStatus(AttendancePermissionStatus.APPROVED);
        permission.setApprovedBy(approver);
        permission.setApprovedAt(Instant.now());
        permission = permissionRepository.save(permission);

        // Apply recalculation to attendance record while preserving original punch timestamps
        applyPermissionToAttendance(permission);

        return AttendancePermissionResponse.fromEntity(permission);
    }

    @Transactional
    public AttendancePermissionResponse rejectPermission(Long id, AttendancePermissionRejectRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        AttendancePermission permission = permissionRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance permission not found with ID: " + id));

        if (permission.getStatus() != AttendancePermissionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING permissions can be rejected (current: " + permission.getStatus() + ").");
        }

        String rejectionReason = (request != null && request.getReason() != null) ? request.getReason() : "Rejected by approver";
        permission.setStatus(AttendancePermissionStatus.REJECTED);
        permission.setRejectionReason(rejectionReason);
        permission = permissionRepository.save(permission);

        return AttendancePermissionResponse.fromEntity(permission);
    }

    @Transactional
    public AttendancePermissionResponse cancelPermission(Long id, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        AttendancePermission permission = permissionRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance permission not found with ID: " + id));

        if (permission.getStatus() != AttendancePermissionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING permissions can be cancelled (current: " + permission.getStatus() + ").");
        }

        permission.setStatus(AttendancePermissionStatus.CANCELLED);
        permission.setRejectionReason(reason != null ? reason : "Cancelled by requester");
        permission = permissionRepository.save(permission);

        if (approvalFacade != null && permission.getWorkflowInstanceId() != null) {
            try {
                approvalFacade.cancel(
                        WorkflowType.ATTENDANCE_PERMISSION,
                        "ATTENDANCE_PERMISSION",
                        String.valueOf(permission.getId()),
                        reason != null ? reason : "Cancelled"
                );
            } catch (Exception e) {
                log.warn("Could not cancel workflow instance for permission {}: {}", permission.getId(), e.getMessage());
            }
        }

        return AttendancePermissionResponse.fromEntity(permission);
    }

    @Transactional
    public void applyPermissionToAttendance(AttendancePermission permission) {
        if (permission == null || permission.getEmployee() == null || permission.getAttendanceDate() == null) {
            return;
        }

        Long orgId = permission.getOrganization().getId();
        Long employeeId = permission.getEmployee().getId();
        LocalDate date = permission.getAttendanceDate();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, date).orElse(null);
        if (attendance == null) {
            log.info("No attendance record found for employee {} on date {} to apply permission {}.", employeeId, date, permission.getId());
            return;
        }

        AttendancePolicy policy = policyService.getActivePolicy(orgId);

        AttendancePermissionType type = permission.getPermissionType();
        int requestedMinutes = permission.getRequestedMinutes() != null ? permission.getRequestedMinutes() : 0;

        // Apply permission adjustments while NEVER mutating checkInTime, checkOutTime, punchInTime, punchOutTime
        if (type == AttendancePermissionType.LATE_ARRIVAL || type == AttendancePermissionType.MISSING_PUNCH ||
            type == AttendancePermissionType.MISSED_CHECK_IN || type == AttendancePermissionType.ATTENDANCE_CORRECTION) {
            attendance.setLateStatus(AttendanceLateStatus.EXCUSED);
            attendance.setIsLate(false);
        }

        if (type == AttendancePermissionType.EARLY_EXIT || type == AttendancePermissionType.MISSING_PUNCH ||
            type == AttendancePermissionType.MISSED_CHECK_OUT || type == AttendancePermissionType.ATTENDANCE_CORRECTION) {
            attendance.setEarlyExitStatus(AttendanceEarlyExitStatus.EXCUSED);
            attendance.setIsEarlyCheckout(false);
        }

        int currentPermMinutes = attendance.getPermissionMinutes() != null ? attendance.getPermissionMinutes() : 0;
        attendance.setPermissionMinutes(currentPermMinutes + requestedMinutes);

        // Recalculate payable minutes
        recalculatePayableMinutes(attendance, policy);

        // Ensure status is PRESENT (or leave unchanged if holiday/leave)
        if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT || attendance.getAttendanceStatus() == null) {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        attendanceRepository.save(attendance);
        permission.setStatus(AttendancePermissionStatus.APPLIED);
        permissionRepository.save(permission);
        log.info("Successfully applied permission {} to attendance record {} for employee {} on date {}",
                permission.getId(), attendance.getId(), employeeId, date);
    }

    public void recalculatePayableMinutes(Attendance attendance, AttendancePolicy policy) {
        if (attendance == null) return;
        int standardMinutes = (policy != null && policy.getMinimumWorkingMinutes() != null)
                ? policy.getMinimumWorkingMinutes()
                : 480;

        int unexcusedLate = (attendance.getLateStatus() == AttendanceLateStatus.UNEXCUSED)
                ? (attendance.getLateByMinutes() != null ? attendance.getLateByMinutes() : 0)
                : 0;

        int unexcusedEarly = (attendance.getEarlyExitStatus() == AttendanceEarlyExitStatus.UNEXCUSED)
                ? (attendance.getEarlyByMinutes() != null ? attendance.getEarlyByMinutes() : 0)
                : 0;

        int calculatedPayable = Math.max(0, standardMinutes - unexcusedLate - unexcusedEarly);
        attendance.setPayableMinutes(calculatedPayable);
    }

    private Employee resolveTargetEmployee(Long requestedEmployeeId, Long orgId) {
        if (requestedEmployeeId != null) {
            return employeeRepository.findByIdAndOrganizationId(requestedEmployeeId, orgId)
                    .orElseThrow(() -> new AttendanceNotFoundException("Target employee not found with ID: " + requestedEmployeeId));
        }
        return resolveCurrentEmployee();
    }

    public Employee resolveCurrentEmployee() {
        Long organizationId = TenantContext.requireOrganizationId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated security context found.");
        }

        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.example.ems.security.dto.AuthPrincipal authPrincipal) {
            email = authPrincipal.getEmail();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String strPrincipal) {
            email = strPrincipal;
        }

        if (email != null && !email.isBlank()) {
            Employee employee = employeeRepository.findByEmailAndOrganizationId(email, organizationId).orElse(null);
            if (employee != null) return employee;
        }

        throw new SecurityException("Could not resolve authenticated employee profile for tenant " + organizationId);
    }

    private String resolveCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof com.example.ems.security.dto.AuthPrincipal principal && principal.getEmail() != null) {
                return principal.getEmail();
            }
            if (auth.getName() != null && !auth.getName().isBlank()) {
                return auth.getName();
            }
        }
        return "APPROVER";
    }
}
