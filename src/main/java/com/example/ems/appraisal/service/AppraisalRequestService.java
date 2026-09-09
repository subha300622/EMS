package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalRequestResponseDto;
import com.example.ems.appraisal.dto.CreateEmployeeAppraisalRequestDto;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.AppraisalConfigurationRepository;
import com.example.ems.appraisal.repository.AppraisalRequestReasonRepository;
import com.example.ems.appraisal.repository.AppraisalRequestRepository;
import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import com.example.ems.appraisal.repository.AppraisalRepository;

@Service
public class AppraisalRequestService {

    @Autowired
    private AppraisalRequestRepository requestRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private ApprovalFacade approvalFacade;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    private static final Set<AppraisalRequestStatus> ACTIVE_REQUEST_STATUSES = Set.of(
            AppraisalRequestStatus.DRAFT,
            AppraisalRequestStatus.SUBMITTED,
            AppraisalRequestStatus.UNDER_REVIEW,
            AppraisalRequestStatus.MORE_INFORMATION_REQUIRED,
            AppraisalRequestStatus.RESUBMITTED
    );

    @Transactional
    public AppraisalRequestResponseDto createDraftRequest(CreateEmployeeAppraisalRequestDto dto, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        validateEligibilityAndConfig(employee, orgId);

        // Check duplicate active request
        if (requestRepository.existsActiveRequest(orgId, employee.getId(), ACTIVE_REQUEST_STATUSES)) {
            throw new IllegalStateException("You already have an active appraisal request in progress.");
        }

        // Check active in-progress appraisal
        List<Appraisal> existingAppraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId());
        boolean hasActiveAppraisal = existingAppraisals.stream()
                .anyMatch(a -> a.getStatus() == AppraisalStatus.CREATED || a.getStatus() == AppraisalStatus.STAGE_REVIEW);
        if (hasActiveAppraisal) {
            throw new IllegalStateException("You already have an active appraisal in progress.");
        }

        AppraisalRequestReason reason = reasonRepository.findByIdAndOrganizationId(dto.getReasonId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request reason not found with ID: " + dto.getReasonId()));

        if (!reason.isActive()) {
            throw new IllegalArgumentException("Selected appraisal request reason is inactive.");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));

        AppraisalRequest request = new AppraisalRequest();
        request.setOrganization(org);
        request.setEmployee(employee);
        request.setReason(reason);
        request.setJustification(dto.getJustification().trim());
        request.setStatus(AppraisalRequestStatus.DRAFT);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return mapToDto(requestRepository.save(request));
    }

    @Transactional
    public AppraisalRequestResponseDto submitRequest(Long requestId, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequest request = requestRepository.findByIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request not found with ID: " + requestId));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You are not authorized to submit this appraisal request.");
        }

        if (request.getStatus() != AppraisalRequestStatus.DRAFT && request.getStatus() != AppraisalRequestStatus.MORE_INFORMATION_REQUIRED) {
            throw new IllegalStateException("Only requests in DRAFT or MORE_INFORMATION_REQUIRED status can be submitted. Current status: " + request.getStatus());
        }

        validateEligibilityAndConfig(employee, orgId);

        // Check or create approval workflow instance
        ApprovalContext approvalContext = new ApprovalContext();
        approvalContext.setModule("APPRAISAL_REQUEST");
        approvalContext.setResourceId(String.valueOf(request.getId()));
        approvalContext.setEmployeeId(employee.getEmployeeId() != null ? employee.getEmployeeId() : String.valueOf(employee.getId()));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reasonCode", request.getReason().getCode());
        metadata.put("reasonName", request.getReason().getName());
        metadata.put("justification", request.getJustification());
        metadata.put("organizationId", orgId);
        if (employee.getDepartment() != null) {
            metadata.put("department", employee.getDepartment());
        }
        approvalContext.setMetadata(metadata);

        ApprovalWorkflowInstance instance = approvalFacade.startApproval(approvalContext);

        request.setStatus(AppraisalRequestStatus.UNDER_REVIEW);
        if (instance != null) {
            request.setApprovalInstanceId(instance.getWorkflowInstanceId());
        }
        request.setSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return mapToDto(requestRepository.save(request));
    }

    @Transactional
    public AppraisalRequestResponseDto createAndSubmitRequest(CreateEmployeeAppraisalRequestDto dto, Employee employee) {
        AppraisalRequestResponseDto draft = createDraftRequest(dto, employee);
        return submitRequest(draft.getId(), employee);
    }

    @Transactional
    public AppraisalRequestResponseDto withdrawRequest(Long requestId, String reason, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequest request = requestRepository.findByIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request not found with ID: " + requestId));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You are not authorized to withdraw this appraisal request.");
        }

        if (request.getStatus() == AppraisalRequestStatus.APPROVED ||
            request.getStatus() == AppraisalRequestStatus.REJECTED ||
            request.getStatus() == AppraisalRequestStatus.WITHDRAWN ||
            request.getStatus() == AppraisalRequestStatus.APPRAISAL_CREATED) {
            throw new IllegalStateException("Request cannot be withdrawn in status: " + request.getStatus());
        }

        // Cancel workflow if running
        if (request.getApprovalInstanceId() != null) {
            try {
                approvalFacade.cancel(WorkflowType.APPRAISAL_REQUEST, "APPRAISAL_REQUEST", String.valueOf(request.getId()), reason);
            } catch (Exception ignored) {}
        }

        request.setStatus(AppraisalRequestStatus.WITHDRAWN);
        request.setWithdrawnAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return mapToDto(requestRepository.save(request));
    }

    @Transactional
    public AppraisalRequestResponseDto resubmitRequest(Long requestId, String additionalJustification, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequest request = requestRepository.findByIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request not found with ID: " + requestId));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You are not authorized to resubmit this appraisal request.");
        }

        if (request.getStatus() != AppraisalRequestStatus.MORE_INFORMATION_REQUIRED && request.getStatus() != AppraisalRequestStatus.DRAFT) {
            throw new IllegalStateException("Only requests in MORE_INFORMATION_REQUIRED or DRAFT status can be resubmitted. Current status: " + request.getStatus());
        }

        if (additionalJustification != null && !additionalJustification.isBlank()) {
            request.setJustification(request.getJustification() + "\n\n[Additional Information]: " + additionalJustification.trim());
        }

        request.setStatus(AppraisalRequestStatus.UNDER_REVIEW);
        request.setUpdatedAt(LocalDateTime.now());

        if (request.getApprovalInstanceId() != null) {
            try {
                approvalFacade.resubmit(WorkflowType.APPRAISAL_REQUEST, "APPRAISAL_REQUEST", String.valueOf(request.getId()), employee, Map.of("organizationId", orgId));
            } catch (Exception ignored) {}
        }

        return mapToDto(requestRepository.save(request));
    }

    public List<AppraisalRequestResponseDto> getMyRequests(Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        return requestRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AppraisalRequestResponseDto> getAllRequests() {
        Long orgId = TenantContext.requireOrganizationId();
        return requestRepository.findByOrganizationId(orgId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AppraisalRequestResponseDto getRequestById(Long requestId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalRequest request = requestRepository.findByIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal request not found with ID: " + requestId));
        return mapToDto(request);
    }

    private void validateEligibilityAndConfig(Employee employee, Long orgId) {
        if (employee == null) {
            throw new IllegalArgumentException("Authenticated employee context is required");
        }

        // Check active configuration
        Optional<AppraisalConfiguration> configOpt = configRepository.findByOrganizationId(orgId);
        int minServiceMonths = 6;
        if (configOpt.isPresent()) {
            AppraisalConfiguration config = configOpt.get();
            if (!config.isEmployeeRequestEnabled()) {
                throw new IllegalStateException("Employee appraisal requests are currently disabled for this organization.");
            }
            if (config.getInitiationMode() == AppraisalInitiationMode.HR_ONLY) {
                throw new IllegalStateException("Organization policy is configured for HR_ONLY appraisal initiation.");
            }
            if (config.getMinServiceMonths() != null) {
                minServiceMonths = config.getMinServiceMonths();
            }
        }

        // Validate employee service duration
        if (employee.getJoiningDate() != null && minServiceMonths > 0) {
            long monthsOfService = ChronoUnit.MONTHS.between(employee.getJoiningDate(), LocalDate.now());
            if (monthsOfService < minServiceMonths) {
                throw new IllegalStateException("Minimum service of " + minServiceMonths + " months is required. Your current service is " + monthsOfService + " months.");
            }
        }
    }

    public AppraisalRequestResponseDto mapToDto(AppraisalRequest r) {
        AppraisalRequestResponseDto dto = new AppraisalRequestResponseDto();
        dto.setId(r.getId());
        if (r.getEmployee() != null) {
            dto.setEmployeeId(r.getEmployee().getId());
            dto.setEmployeeName(r.getEmployee().getFullName());
            dto.setEmployeeEmail(r.getEmployee().getEmail());
        }
        if (r.getReason() != null) {
            dto.setReasonId(r.getReason().getId());
            dto.setReasonCode(r.getReason().getCode());
            dto.setReasonName(r.getReason().getName());
        }
        dto.setJustification(r.getJustification());
        dto.setStatus(r.getStatus());
        dto.setApprovalInstanceId(r.getApprovalInstanceId());
        dto.setAppraisalId(r.getAppraisalId());
        dto.setSubmittedAt(r.getSubmittedAt());
        dto.setApprovedAt(r.getApprovedAt());
        dto.setRejectedAt(r.getRejectedAt());
        dto.setWithdrawnAt(r.getWithdrawnAt());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
