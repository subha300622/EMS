package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeExitService {

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ExitClearanceService exitClearanceService;

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired
    private ExitAuthorizationService exitAuthService;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRls() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {
            }
        }
    }

    @Transactional
    public ExitResponse createExitRequest(User currentUser, CreateExitRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (employee.getOrganization() == null || !orgId.equals(employee.getOrganization().getId())) {
            throw new ResourceNotFoundException("Employee not found in organization ID: " + orgId);
        }

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalStateException("Employee is not currently active: " + employee.getFullName());
        }

        List<EmployeeExit> existingActive = exitRepository.findActiveExitsForEmployee(orgId, employee.getId());
        if (!existingActive.isEmpty()) {
            throw new IllegalStateException(
                    "An active exit request is already in progress for employee: " + employee.getFullName());
        }

        LocalDate resignationDate = request.getResignationDate() != null ? request.getResignationDate()
                : LocalDate.now();
        LocalDate requestedLwd = request.getRequestedLastWorkingDate() != null ? request.getRequestedLastWorkingDate()
                : resignationDate.plusDays(30);

        EmployeeExit exit = new EmployeeExit();
        exit.setOrganization(employee.getOrganization());
        exit.setEmployee(employee);
        exit.setExitType(request.getExitType() != null ? request.getExitType().trim().toUpperCase() : "RESIGNATION");
        exit.setResignationDate(resignationDate);
        exit.setRequestedLastWorkingDate(requestedLwd);
        exit.setLastWorkingDate(requestedLwd);
        exit.setReason(request.getReason());
        exit.setRemarks(request.getRemarks());
        exit.setStatus("MANAGER_APPROVAL_PENDING");
        exit.setReportingManager(employee.getManager());
        exit.setCreatedAt(LocalDateTime.now());
        exit.setUpdatedAt(LocalDateTime.now());

        EmployeeExit saved = exitRepository.save(exit);

        // Start Approval Workflow Instance for Manager Approval
        try {
            approvalWorkflowEngineService.startWorkflow(
                    WorkflowType.EMPLOYEE_EXIT,
                    "EMPLOYEE_EXIT",
                    saved.getId().toString(),
                    employee,
                    null);
        } catch (Exception e) {
            // Fallback gracefully if auto-start encountered issue
        }

        return new ExitResponse(
                saved.getId(),
                employee.getId(),
                employee.getFullName(),
                employee.getEmployeeId(),
                saved.getStatus(),
                saved.getRequestedLastWorkingDate(),
                saved.getLastWorkingDate(),
                saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public ExitDetailResponse getExitById(User currentUser, Long exitId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        EmployeeExit exit = exitRepository.findByIdAndOrganizationId(exitId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit request not found with ID: " + exitId));

        exitAuthService.assertCanViewExit(currentUser, exit);

        List<ExitClearanceDto> clearances = exitClearanceService.getClearancesForExit(currentUser, exitId);
        return mapToDetailDto(exit, clearances);
    }

    @Transactional(readOnly = true)
    public Page<ExitResponse> getExits(User currentUser, String status, String search, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Page<EmployeeExit> exits;
        if (cleanSearch == null && cleanStatus == null) {
            exits = exitRepository.findByOrganizationId(orgId, pageable);
        } else if (cleanSearch == null) {
            exits = exitRepository.findByOrganizationIdAndStatus(orgId, cleanStatus, pageable);
        } else {
            exits = exitRepository.searchExits(orgId, cleanStatus, cleanSearch, pageable);
        }

        return exits.map(e -> new ExitResponse(
                e.getId(),
                e.getEmployee().getId(),
                e.getEmployee().getFullName(),
                e.getEmployee().getEmployeeId(),
                e.getStatus(),
                e.getRequestedLastWorkingDate(),
                e.getLastWorkingDate(),
                e.getCreatedAt()));
    }

    @Transactional
    public OffboardingInitiationResponse initiateOffboarding(User currentUser, Long exitId,
            HrOffboardingRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        EmployeeExit exit = exitRepository.findByIdAndOrganizationId(exitId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit request not found with ID: " + exitId));

        // Enforce capability authorization
        exitAuthService.assertCanManageExit(currentUser, exit);

        // Strict State Machine Lifecycle Guards
        if ("SETTLEMENT_COMPLETED".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException("Cannot initiate offboarding: Exit has already completed settlement.");
        }
        if ("CLEARANCE_PENDING".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException(
                    "Cannot initiate offboarding: Offboarding is already in progress with pending clearances.");
        }
        if ("REJECTED".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException("Cannot initiate offboarding: Exit request has been rejected.");
        }

        // Validate that Manager Approval is complete
        boolean isManagerApproved = "HR_OFFBOARDING_PENDING".equalsIgnoreCase(exit.getStatus())
                || "MANAGER_APPROVED".equalsIgnoreCase(exit.getStatus());

        boolean isSuperAdmin = currentUser != null && currentUser.getRole() != null
                && "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

        if (!isManagerApproved && !isSuperAdmin) {
            throw new IllegalStateException("Cannot initiate HR offboarding: Exit request is in status '"
                    + exit.getStatus() + "'. Manager approval is required.");
        }

        if (request.getLastWorkingDate() != null) {
            exit.setLastWorkingDate(request.getLastWorkingDate());
        }
        if (request.getNoticePeriodDays() != null) {
            exit.setNoticePeriodDays(request.getNoticePeriodDays());
        }
        if (request.getNoticeServedDays() != null) {
            exit.setNoticeServedDays(request.getNoticeServedDays());
        }
        if (request.getRemarks() != null) {
            exit.setRemarks(request.getRemarks());
        }

        // Generate the 4 department clearance records
        List<ExitClearance> clearances = exitClearanceService.initializeClearancesForExit(exit);
        exit.setStatus("CLEARANCE_PENDING");
        exit.setUpdatedAt(LocalDateTime.now());
        EmployeeExit saved = exitRepository.save(exit);

        List<ExitClearanceDto> clearanceDtos = clearances.stream()
                .map(exitClearanceService::mapToDto)
                .collect(Collectors.toList());

        return new OffboardingInitiationResponse(
                saved.getId(),
                saved.getEmployee().getId(),
                saved.getStatus(),
                saved.getLastWorkingDate(),
                saved.getNoticePeriodDays(),
                saved.getNoticeServedDays(),
                clearanceDtos);
    }

    private ExitDetailResponse mapToDetailDto(EmployeeExit exit, List<ExitClearanceDto> clearances) {
        ExitDetailResponse dto = new ExitDetailResponse();
        dto.setExitId(exit.getId());
        dto.setEmployeeId(exit.getEmployee().getId());
        dto.setEmployeeName(exit.getEmployee().getFullName());
        dto.setEmployeeCode(exit.getEmployee().getEmployeeId());
        dto.setDepartment(exit.getEmployee().getDepartment());
        dto.setDesignation(exit.getEmployee().getDesignation());
        dto.setExitType(exit.getExitType());
        dto.setStatus(exit.getStatus());
        dto.setResignationDate(exit.getResignationDate());
        dto.setRequestedLastWorkingDate(exit.getRequestedLastWorkingDate());
        dto.setLastWorkingDate(exit.getLastWorkingDate());
        dto.setNoticePeriodDays(exit.getNoticePeriodDays());
        dto.setNoticeServedDays(exit.getNoticeServedDays());
        dto.setReason(exit.getReason());
        dto.setRemarks(exit.getRemarks());
        if (exit.getReportingManager() != null) {
            dto.setReportingManagerId(exit.getReportingManager().getId());
            dto.setReportingManagerName(exit.getReportingManager().getFullName());
        }
        dto.setClearances(clearances);
        dto.setCreatedAt(exit.getCreatedAt());
        dto.setUpdatedAt(exit.getUpdatedAt());
        return dto;
    }
}
