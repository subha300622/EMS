package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalConfigurationDto;
import com.example.ems.appraisal.dto.AppraisalRequestReasonDto;
import com.example.ems.appraisal.dto.CreateRequestReasonDto;
import com.example.ems.appraisal.dto.ReviewStageConfigurationDto;
import com.example.ems.appraisal.dto.SaveAppraisalConfigurationRequest;
import com.example.ems.appraisal.entity.AppraisalConfiguration;
import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import com.example.ems.appraisal.entity.AppraisalRequestReason;
import com.example.ems.appraisal.entity.AppraisalReviewStage;
import com.example.ems.appraisal.repository.AppraisalConfigurationRepository;
import com.example.ems.appraisal.repository.AppraisalRequestReasonRepository;
import com.example.ems.appraisal.repository.AppraisalReviewStageRepository;
import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.approval.repository.ApprovalWorkflowDefinitionRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppraisalConfigurationService {

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalReviewStageRepository reviewStageRepository;

    @Autowired
    private ApprovalWorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    public List<ReviewStageConfigurationDto> getDefaultReviewStages() {
        return List.of(
                new ReviewStageConfigurationDto(1, "Team Lead Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "Manager Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(3, "HR Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(4, "Director Approval", "APPRAISAL_APPROVE", true),
                new ReviewStageConfigurationDto(5, "Finance Review", "APPRAISAL_REVIEW", true)
        );
    }

    @Transactional(readOnly = true)
    public AppraisalConfigurationDto getConfiguration() {
        Long orgId = TenantContext.requireOrganizationId();
        Optional<AppraisalConfiguration> configOpt = configRepository.findByOrganizationId(orgId);

        List<AppraisalRequestReasonDto> reasons = reasonRepository.findByOrganizationId(orgId)
                .stream()
                .map(r -> new AppraisalRequestReasonDto(r.getId(), r.getCode(), r.getName(), r.getDescription(), r.isActive()))
                .collect(Collectors.toList());

        List<AppraisalReviewStage> storedStages = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);
        List<ReviewStageConfigurationDto> reviewStages = storedStages.isEmpty()
                ? getDefaultReviewStages()
                : storedStages.stream()
                .map(s -> {
                    ReviewStageConfigurationDto dto = new ReviewStageConfigurationDto(
                            s.getStageOrder(),
                            s.getStageName(),
                            s.getRequiredPermission(),
                            s.isRequired()
                    );
                    dto.setId(s.getId());
                    dto.setWeightage(s.getWeightage());
                    return dto;
                })
                .collect(Collectors.toList());

        if (configOpt.isEmpty()) {
            // Return sensible active defaults
            AppraisalConfigurationDto defaultDto = new AppraisalConfigurationDto();
            defaultDto.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
            defaultDto.setEmployeeRequestEnabled(true);
            defaultDto.setMinServiceMonths(6);
            defaultDto.setMinGapMonths(6);
            defaultDto.setActive(true);
            defaultDto.setAllowedRequestReasons(reasons);
            defaultDto.setReviewStages(reviewStages);
            return defaultDto;
        }

        AppraisalConfiguration config = configOpt.get();
        AppraisalConfigurationDto dto = new AppraisalConfigurationDto();
        dto.setId(config.getId());
        dto.setInitiationMode(config.getInitiationMode());
        dto.setEmployeeRequestEnabled(config.isEmployeeRequestEnabled());
        dto.setApprovalWorkflowId(config.getApprovalWorkflow() != null ? config.getApprovalWorkflow().getId() : null);
        dto.setReviewWorkflowId(config.getReviewWorkflow() != null ? config.getReviewWorkflow().getId() : null);
        dto.setMinServiceMonths(config.getMinServiceMonths());
        dto.setMinGapMonths(config.getMinGapMonths());
        dto.setActive(config.isActive());
        dto.setAllowedRequestReasons(reasons);
        dto.setReviewStages(reviewStages);
        return dto;
    }

    @Transactional
    public AppraisalConfigurationDto saveOrUpdateConfiguration(SaveAppraisalConfigurationRequest request, Employee currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));

        AppraisalConfiguration config = configRepository.findByOrganizationId(orgId)
                .orElseGet(() -> {
                    AppraisalConfiguration c = new AppraisalConfiguration();
                    c.setOrganization(org);
                    c.setCreatedBy(currentUser != null ? currentUser.getId() : null);
                    c.setCreatedAt(LocalDateTime.now());
                    return c;
                });

        config.setInitiationMode(request.getInitiationMode());
        config.setEmployeeRequestEnabled(request.isEmployeeRequestEnabled());

        if (request.getMinServiceMonths() != null) {
            if (request.getMinServiceMonths() < 0) {
                throw new IllegalArgumentException("minServiceMonths cannot be negative");
            }
            config.setMinServiceMonths(request.getMinServiceMonths());
        }
        if (request.getMinGapMonths() != null) {
            if (request.getMinGapMonths() < 0) {
                throw new IllegalArgumentException("minGapMonths cannot be negative");
            }
            config.setMinGapMonths(request.getMinGapMonths());
        }

        // Validate approval workflow (must belong to this org and be active)
        if (request.getApprovalWorkflowId() != null) {
            ApprovalWorkflowDefinition wf = workflowDefinitionRepository.findById(request.getApprovalWorkflowId())
                    .orElseThrow(() -> new IllegalArgumentException("Approval workflow definition not found with ID: " + request.getApprovalWorkflowId()));
            if (wf.getOrganization() != null && !wf.getOrganization().getId().equals(orgId)) {
                throw new SecurityException("Cross-tenant violation: Workflow does not belong to your organization.");
            }
            config.setApprovalWorkflow(wf);
        } else {
            config.setApprovalWorkflow(null);
        }

        // Validate review workflow (if provided)
        if (request.getReviewWorkflowId() != null) {
            ApprovalWorkflowDefinition reviewWf = workflowDefinitionRepository.findById(request.getReviewWorkflowId())
                    .orElseThrow(() -> new IllegalArgumentException("Review workflow definition not found with ID: " + request.getReviewWorkflowId()));
            if (reviewWf.getOrganization() != null && !reviewWf.getOrganization().getId().equals(orgId)) {
                throw new SecurityException("Cross-tenant violation: Review workflow does not belong to your organization.");
            }
            config.setReviewWorkflow(reviewWf);
        } else {
            config.setReviewWorkflow(null);
        }

        config.setActive(true);
        config.setUpdatedBy(currentUser != null ? currentUser.getId() : null);
        config.setUpdatedAt(LocalDateTime.now());

        config = configRepository.save(config);

        // Process request reasons if supplied
        if (request.getAllowedRequestReasons() != null && !request.getAllowedRequestReasons().isEmpty()) {
            Set<String> seenCodes = new HashSet<>();
            for (CreateRequestReasonDto reasonDto : request.getAllowedRequestReasons()) {
                if (reasonDto.getCode() == null || reasonDto.getCode().isBlank()) continue;
                String code = reasonDto.getCode().trim().toUpperCase();
                if (!seenCodes.add(code)) {
                    throw new IllegalArgumentException("Duplicate reason code '" + code + "' in request reasons list");
                }
                Optional<AppraisalRequestReason> existingOpt = reasonRepository.findByOrganizationIdAndCodeIgnoreCase(orgId, code);
                if (existingOpt.isPresent()) {
                    AppraisalRequestReason existing = existingOpt.get();
                    if (reasonDto.getName() != null) existing.setName(reasonDto.getName());
                    if (reasonDto.getDescription() != null) existing.setDescription(reasonDto.getDescription());
                    existing.setActive(true);
                    existing.setUpdatedAt(LocalDateTime.now());
                    reasonRepository.save(existing);
                } else {
                    AppraisalRequestReason newReason = new AppraisalRequestReason();
                    newReason.setOrganization(org);
                    newReason.setCode(code);
                    newReason.setName(reasonDto.getName() != null ? reasonDto.getName() : code);
                    newReason.setDescription(reasonDto.getDescription());
                    newReason.setActive(true);
                    newReason.setCreatedAt(LocalDateTime.now());
                    newReason.setUpdatedAt(LocalDateTime.now());
                    reasonRepository.save(newReason);
                }
            }
        }

        // Process review stages if supplied
        if (request.getReviewStages() != null) {
            if (request.getReviewStages().isEmpty()) {
                throw new IllegalArgumentException("reviewStages cannot be empty if specified");
            }
            reviewStageRepository.deleteByOrganizationId(orgId);
            reviewStageRepository.flush();

            Set<Integer> seenOrders = new HashSet<>();
            Set<String> seenNames = new HashSet<>();
            for (ReviewStageConfigurationDto stageDto : request.getReviewStages()) {
                if (stageDto.getStageOrder() == null || stageDto.getStageOrder() <= 0) {
                    throw new IllegalArgumentException("stageOrder must be a positive integer");
                }
                if (!seenOrders.add(stageDto.getStageOrder())) {
                    throw new IllegalArgumentException("Duplicate stageOrder " + stageDto.getStageOrder() + " in reviewStages");
                }
                if (stageDto.getStageName() != null) {
                    String nameTrimmed = stageDto.getStageName().trim();
                    if (nameTrimmed.isEmpty()) {
                        throw new IllegalArgumentException("Review stage name cannot be blank");
                    }
                    if (!seenNames.add(nameTrimmed.toLowerCase())) {
                        throw new IllegalArgumentException("Duplicate stageName '" + stageDto.getStageName() + "' in reviewStages");
                    }
                }
                if (stageDto.getPermission() != null && stageDto.getPermission().isBlank()) {
                    throw new IllegalArgumentException("Review stage permission cannot be blank");
                }
                if (stageDto.getWeightage() != null && stageDto.getWeightage() < 0) {
                    throw new IllegalArgumentException("Review stage weightage cannot be negative");
                }

                AppraisalReviewStage stage = new AppraisalReviewStage();
                stage.setOrganization(org);
                stage.setConfiguration(config);
                stage.setStageOrder(stageDto.getStageOrder());
                stage.setStageName(stageDto.getStageName() != null ? stageDto.getStageName() : "Stage " + stageDto.getStageOrder() + " Review");
                stage.setRequiredPermission(stageDto.getPermission() != null && !stageDto.getPermission().isBlank() ? stageDto.getPermission() : "APPRAISAL_REVIEW");
                stage.setRequired(stageDto.isRequired());
                stage.setWeightage(stageDto.getWeightage() != null ? stageDto.getWeightage() : 1.0);
                stage.setCreatedAt(LocalDateTime.now());
                stage.setUpdatedAt(LocalDateTime.now());
                reviewStageRepository.save(stage);
            }
        }

        return getConfiguration();
    }
}
