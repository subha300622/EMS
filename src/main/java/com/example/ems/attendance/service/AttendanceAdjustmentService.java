package com.example.ems.attendance.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceAdjustment;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
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
public class AttendanceAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceAdjustmentService.class);

    @Autowired
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceCorrectionService attendanceCorrectionService;

    @Autowired(required = false)
    private ApprovalFacade approvalFacade;

    @Transactional
    public AdjustmentResponseDto createAdjustment(Long attendanceId, CreateAdjustmentRequest request) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("attendanceId is mandatory.");
        }
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is mandatory.");
        }
        if (request.getRequestedCheckInTime() == null && request.getRequestedCheckOutTime() == null) {
            throw new IllegalArgumentException("At least one requested timestamp (check-in or check-out) must be provided.");
        }

        Long organizationId = TenantContext.requireOrganizationId();
        Attendance attendance = attendanceRepository.findByIdAndOrganizationId(attendanceId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found with ID: " + attendanceId));

        if (adjustmentRepository.existsByAttendanceIdAndStatus(attendance.getId(), AttendanceAdjustmentStatus.PENDING)) {
            throw new IllegalStateException("An active pending adjustment request already exists for this attendance record.");
        }

        Instant effectiveCheckIn = request.getRequestedCheckInTime() != null ? request.getRequestedCheckInTime() : attendance.getCheckInTime();
        Instant effectiveCheckOut = request.getRequestedCheckOutTime() != null ? request.getRequestedCheckOutTime() : attendance.getCheckOutTime();

        if (effectiveCheckIn != null && effectiveCheckOut != null && !effectiveCheckOut.isAfter(effectiveCheckIn)) {
            throw new IllegalArgumentException("Requested check-out time must be after check-in time.");
        }

        Employee employee = attendance.getEmployee();
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setAttendance(attendance);
        adjustment.setEmployee(employee);
        adjustment.setOrganization(attendance.getOrganization());
        adjustment.setRequestedCheckInTime(request.getRequestedCheckInTime());
        adjustment.setRequestedCheckOutTime(request.getRequestedCheckOutTime());
        adjustment.setReason(request.getReason());
        adjustment.setStatus(AttendanceAdjustmentStatus.PENDING);

        adjustment = adjustmentRepository.save(adjustment);

        // Integrate with central ApprovalFacade
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext();
                context.setModule("ATTENDANCE_ADJUSTMENT");
                context.setResourceId(String.valueOf(adjustment.getId()));
                context.setEmployeeId(String.valueOf(employee != null ? employee.getId() : ""));
                context.setDepartmentId(employee != null && employee.getDepartment() != null ? 1L : null);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    adjustment.setWorkflowInstanceId(String.valueOf(instance.getId()));
                    adjustment = adjustmentRepository.save(adjustment);
                }
            } catch (Exception e) {
                log.warn("Failed to spawn approval workflow instance for adjustment {}: {}", adjustment.getId(), e.getMessage());
            }
        }

        return AdjustmentResponseDto.fromEntity(adjustment);
    }

    @Transactional(readOnly = true)
    public AdjustmentResponseDto getAdjustmentById(Long id) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceAdjustment adjustment = adjustmentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Adjustment request not found with ID: " + id));

        return AdjustmentResponseDto.fromEntity(adjustment);
    }

    @Transactional(readOnly = true)
    public Page<AdjustmentResponseDto> getAdjustments(Long employeeId, AttendanceAdjustmentStatus status, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Long organizationId = TenantContext.requireOrganizationId();
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AttendanceAdjustment> result = adjustmentRepository.findAdjustments(
                organizationId,
                employeeId,
                status,
                fromDate,
                toDate,
                pageable
        );

        return result.map(AdjustmentResponseDto::fromEntity);
    }

    @Transactional
    public AdjustmentResponseDto cancelAdjustment(Long id, String reason) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceAdjustment adjustment = adjustmentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Adjustment request not found with ID: " + id));

        if (adjustment.getStatus() != AttendanceAdjustmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING adjustment requests can be cancelled (current: " + adjustment.getStatus() + ").");
        }

        adjustment.setStatus(AttendanceAdjustmentStatus.CANCELLED);
        adjustment.setManagerNotes(reason != null ? reason : "Cancelled");
        adjustment = adjustmentRepository.save(adjustment);

        if (approvalFacade != null && adjustment.getWorkflowInstanceId() != null) {
            try {
                approvalFacade.cancel(
                        WorkflowType.ATTENDANCE_ADJUSTMENT,
                        "ATTENDANCE_ADJUSTMENT",
                        String.valueOf(adjustment.getId()),
                        reason != null ? reason : "Cancelled"
                );
            } catch (Exception e) {
                log.warn("Failed to cancel central workflow for adjustment {}: {}", adjustment.getId(), e.getMessage());
            }
        }

        return AdjustmentResponseDto.fromEntity(adjustment);
    }

    @Transactional
    public AdjustmentResponseDto approveAdjustment(Long id, AdjustmentApprovalRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceAdjustment adjustment = adjustmentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Adjustment request not found with ID: " + id));

        if (adjustment.getStatus() != AttendanceAdjustmentStatus.PENDING) {
            throw new IllegalStateException("Adjustment request is not in PENDING state (current: " + adjustment.getStatus() + ").");
        }

        String approverName = resolveCurrentUserName();
        adjustment.setStatus(AttendanceAdjustmentStatus.APPROVED);
        adjustment.setApprovedBy(approverName);
        adjustment.setApprovedAt(Instant.now());
        if (request != null && request.getRemarks() != null) {
            adjustment.setManagerNotes(request.getRemarks());
        }
        adjustment = adjustmentRepository.save(adjustment);

        // Apply centralized attendance correction
        if (adjustment.getAttendance() != null) {
            attendanceCorrectionService.applyCorrection(
                    adjustment.getAttendance(),
                    adjustment.getRequestedCheckInTime(),
                    adjustment.getRequestedCheckOutTime(),
                    request != null ? request.getRemarks() : "Adjustment approval",
                    approverName,
                    "ADJUSTMENT"
            );
        }

        return AdjustmentResponseDto.fromEntity(adjustment);
    }

    @Transactional
    public AdjustmentResponseDto rejectAdjustment(Long id, AdjustmentApprovalRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendanceAdjustment adjustment = adjustmentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Adjustment request not found with ID: " + id));

        if (adjustment.getStatus() != AttendanceAdjustmentStatus.PENDING) {
            throw new IllegalStateException("Adjustment request is not in PENDING state (current: " + adjustment.getStatus() + ").");
        }

        adjustment.setStatus(AttendanceAdjustmentStatus.REJECTED);
        String remarks = (request != null && request.getRemarks() != null) ? request.getRemarks() : "Rejected by approver";
        adjustment.setRejectionReason(remarks);
        adjustment.setManagerNotes(remarks);
        adjustment = adjustmentRepository.save(adjustment);

        return AdjustmentResponseDto.fromEntity(adjustment);
    }

    private String resolveCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "APPROVER";
    }
}
