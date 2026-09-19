package com.example.ems.offboarding.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.enums.*;
import com.example.ems.offboarding.repository.*;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OffboardingTemplateService {

    private final OffboardingTemplateRepository templateRepository;
    private final OffboardingClearanceTaskTemplateRepository clearanceTaskRepository;
    private final OffboardingAssetRequirementTemplateRepository assetRequirementRepository;
    private final OffboardingDocumentRequirementTemplateRepository documentRequirementRepository;
    private final OffboardingKtTemplateRepository ktTemplateRepository;
    private final OffboardingInterviewTemplateRepository interviewTemplateRepository;
    private final OffboardingInterviewQuestionRepository interviewQuestionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffboardingTemplateService(
            OffboardingTemplateRepository templateRepository,
            OffboardingClearanceTaskTemplateRepository clearanceTaskRepository,
            OffboardingAssetRequirementTemplateRepository assetRequirementRepository,
            OffboardingDocumentRequirementTemplateRepository documentRequirementRepository,
            OffboardingKtTemplateRepository ktTemplateRepository,
            OffboardingInterviewTemplateRepository interviewTemplateRepository,
            OffboardingInterviewQuestionRepository interviewQuestionRepository,
            ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.clearanceTaskRepository = clearanceTaskRepository;
        this.assetRequirementRepository = assetRequirementRepository;
        this.documentRequirementRepository = documentRequirementRepository;
        this.ktTemplateRepository = ktTemplateRepository;
        this.interviewTemplateRepository = interviewTemplateRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.objectMapper = objectMapper;
    }

    // ==========================================
    // 1. Base Offboarding Template Management
    // ==========================================

    @Transactional
    public OffboardingTemplateResponse createTemplate(OffboardingTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (request.getNoticePeriodDefaultDays() != null && request.getNoticePeriodDefaultDays() < 0) {
            throw new IllegalArgumentException("Notice period default days cannot be negative");
        }

        if (templateRepository.existsByNameAndOrganizationId(request.getName().trim(), orgId)) {
            throw new IllegalArgumentException("Offboarding template with name '" + request.getName().trim() + "' already exists");
        }

        OffboardingTemplate template = new OffboardingTemplate();
        template.setOrganizationId(orgId);
        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        template.setStatus(request.getStatus() != null ? request.getStatus() : OffboardingTemplateStatus.ACTIVE);
        template.setDepartmentIdsJson(writeListJson(request.getDepartmentIds()));
        template.setEmploymentTypesJson(writeListJson(request.getEmploymentTypes()));
        template.setEmployeeTypesJson(writeListJson(request.getEmployeeTypes()));
        template.setNoticePeriodEnabled(request.getNoticePeriodEnabled() != null ? request.getNoticePeriodEnabled() : true);
        template.setNoticePeriodDefaultDays(request.getNoticePeriodDefaultDays() != null ? request.getNoticePeriodDefaultDays() : 30);
        template.setAllowEarlyRelease(request.getAllowEarlyRelease() != null ? request.getAllowEarlyRelease() : true);
        template.setAllowNoticePeriodBuyout(request.getAllowNoticePeriodBuyout() != null ? request.getAllowNoticePeriodBuyout() : true);

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Transactional
    public OffboardingTemplateResponse updateTemplate(Long templateId, OffboardingTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Template name cannot be empty");
            }
            if (!request.getName().trim().equalsIgnoreCase(template.getName())) {
                if (templateRepository.existsByNameAndOrganizationIdAndIdNot(request.getName().trim(), orgId, templateId)) {
                    throw new IllegalArgumentException("Offboarding template with name '" + request.getName().trim() + "' already exists");
                }
                template.setName(request.getName().trim());
            }
        }

        if (request.getNoticePeriodDefaultDays() != null && request.getNoticePeriodDefaultDays() < 0) {
            throw new IllegalArgumentException("Notice period default days cannot be negative");
        }

        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }
        if (request.getDepartmentIds() != null) {
            template.setDepartmentIdsJson(writeListJson(request.getDepartmentIds()));
        }
        if (request.getEmploymentTypes() != null) {
            template.setEmploymentTypesJson(writeListJson(request.getEmploymentTypes()));
        }
        if (request.getEmployeeTypes() != null) {
            template.setEmployeeTypesJson(writeListJson(request.getEmployeeTypes()));
        }
        if (request.getNoticePeriodEnabled() != null) {
            template.setNoticePeriodEnabled(request.getNoticePeriodEnabled());
        }
        if (request.getNoticePeriodDefaultDays() != null) {
            template.setNoticePeriodDefaultDays(request.getNoticePeriodDefaultDays());
        }
        if (request.getAllowEarlyRelease() != null) {
            template.setAllowEarlyRelease(request.getAllowEarlyRelease());
        }
        if (request.getAllowNoticePeriodBuyout() != null) {
            template.setAllowNoticePeriodBuyout(request.getAllowNoticePeriodBuyout());
        }

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Transactional
    public OffboardingTemplateResponse activateTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        if (template.getStatus() == OffboardingTemplateStatus.ACTIVE) {
            throw new IllegalStateException("Template is already active");
        }
        if (template.getStatus() == OffboardingTemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Archived template cannot be activated");
        }

        List<OffboardingClearanceTaskTemplate> tasks = clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        if (tasks.isEmpty()) {
            throw new IllegalStateException("Template configuration is incomplete. At least one clearance task is required for activation.");
        }

        template.setStatus(OffboardingTemplateStatus.ACTIVE);
        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Transactional
    public OffboardingTemplateResponse deactivateTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        if (template.getStatus() == OffboardingTemplateStatus.INACTIVE) {
            throw new IllegalStateException("Template is already inactive");
        }
        if (template.getStatus() == OffboardingTemplateStatus.DRAFT) {
            throw new IllegalStateException("Draft template cannot be deactivated");
        }
        if (template.getStatus() == OffboardingTemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Archived template cannot be deactivated");
        }

        template.setStatus(OffboardingTemplateStatus.INACTIVE);
        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    public OffboardingTemplateResponse getTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));
        return mapToResponse(template);
    }

    public OffboardingTemplateDetailResponse getTemplateDetails(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        OffboardingTemplateDetailResponse detail = new OffboardingTemplateDetailResponse();
        detail.setId(template.getId());
        detail.setOrganizationId(template.getOrganizationId());
        detail.setName(template.getName());
        detail.setDescription(template.getDescription());
        detail.setStatus(template.getStatus());
        detail.setDepartmentIds(readListJson(template.getDepartmentIdsJson()));
        detail.setEmploymentTypes(readListJson(template.getEmploymentTypesJson()));
        detail.setEmployeeTypes(readListJson(template.getEmployeeTypesJson()));
        detail.setNoticePeriodEnabled(template.getNoticePeriodEnabled());
        detail.setNoticePeriodDefaultDays(template.getNoticePeriodDefaultDays());
        detail.setAllowEarlyRelease(template.getAllowEarlyRelease());
        detail.setAllowNoticePeriodBuyout(template.getAllowNoticePeriodBuyout());
        detail.setCreatedAt(template.getCreatedAt());
        detail.setUpdatedAt(template.getUpdatedAt());

        // Sub-resources
        List<OffboardingClearanceTaskTemplate> clearanceTasks = clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        detail.setClearanceTasks(clearanceTasks.stream().map(this::mapClearanceTaskToResponse).collect(Collectors.toList()));

        List<OffboardingAssetRequirementTemplate> assetRequirements = assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        detail.setAssetRequirements(assetRequirements.stream().map(this::mapAssetReqToResponse).collect(Collectors.toList()));

        List<OffboardingDocumentRequirementTemplate> documentRequirements = documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        detail.setDocumentRequirements(documentRequirements.stream().map(this::mapDocReqToResponse).collect(Collectors.toList()));

        ktTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .ifPresent(kt -> detail.setKtTemplate(mapKtToResponse(kt)));

        interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .ifPresent(interview -> {
                    InterviewTemplateResponse interviewResp = mapInterviewToResponse(interview);
                    List<OffboardingInterviewQuestion> questions = interviewQuestionRepository
                            .findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(interview.getId(), orgId);
                    interviewResp.setQuestions(questions.stream().map(this::mapInterviewQuestionToResponse).collect(Collectors.toList()));
                    detail.setInterviewTemplate(interviewResp);
                });

        return detail;
    }

    public Page<OffboardingTemplateResponse> listTemplates(OffboardingTemplateStatus status, String search, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<OffboardingTemplate> page;
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            if (status != null) {
                page = templateRepository.findByOrganizationIdAndStatusAndSearch(orgId, status, pattern, pageable);
            } else {
                page = templateRepository.findByOrganizationIdAndSearch(orgId, pattern, pageable);
            }
        } else {
            if (status != null) {
                page = templateRepository.findByOrganizationIdAndStatus(orgId, status, pageable);
            } else {
                page = templateRepository.findByOrganizationId(orgId, pageable);
            }
        }
        return page.map(this::mapToResponse);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        template.setStatus(OffboardingTemplateStatus.ARCHIVED);
        templateRepository.save(template);
    }

    @Transactional
    public OffboardingTemplateDetailResponse cloneTemplate(Long templateId, String newName) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingTemplate source = templateRepository.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding template not found with ID: " + templateId));

        String cloneName = (newName != null && !newName.isBlank()) ? newName.trim() : source.getName() + " (Copy)";
        if (templateRepository.existsByNameAndOrganizationId(cloneName, orgId)) {
            cloneName = cloneName + " " + System.currentTimeMillis();
        }

        // Clone base template
        OffboardingTemplate target = new OffboardingTemplate();
        target.setOrganizationId(orgId);
        target.setName(cloneName);
        target.setDescription(source.getDescription());
        target.setStatus(OffboardingTemplateStatus.ACTIVE);
        target.setDepartmentIdsJson(source.getDepartmentIdsJson());
        target.setEmploymentTypesJson(source.getEmploymentTypesJson());
        target.setEmployeeTypesJson(source.getEmployeeTypesJson());
        target.setNoticePeriodEnabled(source.getNoticePeriodEnabled());
        target.setNoticePeriodDefaultDays(source.getNoticePeriodDefaultDays());
        target.setAllowEarlyRelease(source.getAllowEarlyRelease());
        target.setAllowNoticePeriodBuyout(source.getAllowNoticePeriodBuyout());
        target = templateRepository.save(target);

        Long newTemplateId = target.getId();

        // Clone clearance tasks
        List<OffboardingClearanceTaskTemplate> sourceTasks = clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        for (OffboardingClearanceTaskTemplate task : sourceTasks) {
            OffboardingClearanceTaskTemplate copy = new OffboardingClearanceTaskTemplate();
            copy.setOrganizationId(orgId);
            copy.setTemplateId(newTemplateId);
            copy.setTaskName(task.getTaskName());
            copy.setDescription(task.getDescription());
            copy.setAssignToType(task.getAssignToType());
            copy.setAssignedUserId(task.getAssignedUserId());
            copy.setDueBeforeLwdDays(task.getDueBeforeLwdDays());
            copy.setPriority(task.getPriority());
            copy.setMandatory(task.getMandatory());
            copy.setApprovalRequired(task.getApprovalRequired());
            copy.setSequence(task.getSequence());
            copy.setActive(task.getActive());
            clearanceTaskRepository.save(copy);
        }

        // Clone asset requirements
        List<OffboardingAssetRequirementTemplate> sourceAssets = assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        for (OffboardingAssetRequirementTemplate asset : sourceAssets) {
            OffboardingAssetRequirementTemplate copy = new OffboardingAssetRequirementTemplate();
            copy.setOrganizationId(orgId);
            copy.setTemplateId(newTemplateId);
            copy.setAssetType(asset.getAssetType());
            copy.setName(asset.getName());
            copy.setDescription(asset.getDescription());
            copy.setReturnRequired(asset.getReturnRequired());
            copy.setConditionCheckRequired(asset.getConditionCheckRequired());
            copy.setSerialNumberVerificationRequired(asset.getSerialNumberVerificationRequired());
            copy.setDueBeforeLwdDays(asset.getDueBeforeLwdDays());
            copy.setAssignedToType(asset.getAssignedToType());
            copy.setAssignedUserId(asset.getAssignedUserId());
            copy.setMandatory(asset.getMandatory());
            copy.setApprovalRequired(asset.getApprovalRequired());
            copy.setSequence(asset.getSequence());
            copy.setActive(asset.getActive());
            assetRequirementRepository.save(copy);
        }

        // Clone document requirements
        List<OffboardingDocumentRequirementTemplate> sourceDocs = documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId);
        for (OffboardingDocumentRequirementTemplate doc : sourceDocs) {
            OffboardingDocumentRequirementTemplate copy = new OffboardingDocumentRequirementTemplate();
            copy.setOrganizationId(orgId);
            copy.setTemplateId(newTemplateId);
            copy.setDocumentType(doc.getDocumentType());
            copy.setDocumentName(doc.getDocumentName());
            copy.setDescription(doc.getDescription());
            copy.setRequired(doc.getRequired());
            copy.setAction(doc.getAction());
            copy.setOwnerType(doc.getOwnerType());
            copy.setOwnerUserId(doc.getOwnerUserId());
            copy.setDueBeforeLwdDays(doc.getDueBeforeLwdDays());
            copy.setEmployeeUploadRequired(doc.getEmployeeUploadRequired());
            copy.setApprovalRequired(doc.getApprovalRequired());
            copy.setSequence(doc.getSequence());
            copy.setActive(doc.getActive());
            documentRequirementRepository.save(copy);
        }

        // Clone KT template
        ktTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId).ifPresent(kt -> {
            OffboardingKtTemplate copy = new OffboardingKtTemplate();
            copy.setOrganizationId(orgId);
            copy.setTemplateId(newTemplateId);
            copy.setRequired(kt.getRequired());
            copy.setTitle(kt.getTitle());
            copy.setDescription(kt.getDescription());
            copy.setAssignToType(kt.getAssignToType());
            copy.setAssignedUserId(kt.getAssignedUserId());
            copy.setEmployeeResponsibilitiesJson(kt.getEmployeeResponsibilitiesJson());
            copy.setHandoverDocumentRequired(kt.getHandoverDocumentRequired());
            copy.setHandoverApprovalRequired(kt.getHandoverApprovalRequired());
            copy.setDueBeforeLwdDays(kt.getDueBeforeLwdDays());
            copy.setPriority(kt.getPriority());
            copy.setMandatory(kt.getMandatory());
            ktTemplateRepository.save(copy);
        });

        // Clone Interview template & questions
        interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId).ifPresent(interview -> {
            OffboardingInterviewTemplate copy = new OffboardingInterviewTemplate();
            copy.setOrganizationId(orgId);
            copy.setTemplateId(newTemplateId);
            copy.setEnabled(interview.getEnabled());
            copy.setConductedByType(interview.getConductedByType());
            copy.setConductedByUserId(interview.getConductedByUserId());
            copy.setMandatory(interview.getMandatory());
            copy.setDueBeforeLwdDays(interview.getDueBeforeLwdDays());
            copy.setAllowAnonymousFeedback(interview.getAllowAnonymousFeedback());
            copy = interviewTemplateRepository.save(copy);

            Long newInterviewId = copy.getId();
            List<OffboardingInterviewQuestion> sourceQuestions = interviewQuestionRepository
                    .findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(interview.getId(), orgId);
            for (OffboardingInterviewQuestion q : sourceQuestions) {
                OffboardingInterviewQuestion qCopy = new OffboardingInterviewQuestion();
                qCopy.setOrganizationId(orgId);
                qCopy.setInterviewTemplateId(newInterviewId);
                qCopy.setQuestion(q.getQuestion());
                qCopy.setQuestionType(q.getQuestionType());
                qCopy.setRequired(q.getRequired());
                qCopy.setOptionsJson(q.getOptionsJson());
                qCopy.setMinValue(q.getMinValue());
                qCopy.setMaxValue(q.getMaxValue());
                qCopy.setSequence(q.getSequence());
                qCopy.setActive(q.getActive());
                interviewQuestionRepository.save(qCopy);
            }
        });

        return getTemplateDetails(newTemplateId);
    }

    // ==========================================
    // 2. Clearance Task Templates
    // ==========================================

    @Transactional
    public ClearanceTaskTemplateResponse addClearanceTask(Long templateId, ClearanceTaskTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        if (request.getTaskName() == null || request.getTaskName().trim().isEmpty()) {
            throw new IllegalArgumentException("Task name is required");
        }
        if (request.getAssignToType() == null) {
            throw new IllegalArgumentException("Assign to type is required");
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        OffboardingClearanceTaskTemplate task = new OffboardingClearanceTaskTemplate();
        task.setOrganizationId(orgId);
        task.setTemplateId(templateId);
        task.setTaskName(request.getTaskName().trim());
        task.setDescription(request.getDescription());
        task.setAssignToType(request.getAssignToType());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setDueBeforeLwdDays(request.getDueBeforeLwdDays() != null ? request.getDueBeforeLwdDays() : 3);
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setMandatory(request.getMandatory() != null ? request.getMandatory() : true);
        task.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : false);
        task.setSequence(request.getSequence() != null ? request.getSequence() : 1);
        task.setActive(request.getActive() != null ? request.getActive() : true);

        task = clearanceTaskRepository.save(task);
        return mapClearanceTaskToResponse(task);
    }

    @Transactional
    public ClearanceTaskTemplateResponse updateClearanceTask(Long templateId, Long taskId, ClearanceTaskTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingClearanceTaskTemplate task = clearanceTaskRepository.findByIdAndOrganizationId(taskId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Clearance task template not found with ID: " + taskId));

        if (!task.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Task does not belong to template with ID: " + templateId);
        }

        if (request.getTaskName() != null) {
            if (request.getTaskName().trim().isEmpty()) {
                throw new IllegalArgumentException("Task name cannot be empty");
            }
            task.setTaskName(request.getTaskName().trim());
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getAssignToType() != null) task.setAssignToType(request.getAssignToType());
        if (request.getAssignedUserId() != null) task.setAssignedUserId(request.getAssignedUserId());
        if (request.getDueBeforeLwdDays() != null) task.setDueBeforeLwdDays(request.getDueBeforeLwdDays());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getMandatory() != null) task.setMandatory(request.getMandatory());
        if (request.getApprovalRequired() != null) task.setApprovalRequired(request.getApprovalRequired());
        if (request.getSequence() != null) task.setSequence(request.getSequence());
        if (request.getActive() != null) task.setActive(request.getActive());

        task = clearanceTaskRepository.save(task);
        return mapClearanceTaskToResponse(task);
    }

    @Transactional
    public void deleteClearanceTask(Long templateId, Long taskId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingClearanceTaskTemplate task = clearanceTaskRepository.findByIdAndOrganizationId(taskId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Clearance task template not found with ID: " + taskId));

        if (!task.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Task does not belong to template with ID: " + templateId);
        }

        clearanceTaskRepository.delete(task);
    }

    public List<ClearanceTaskTemplateResponse> listClearanceTasks(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);
        return clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId)
                .stream().map(this::mapClearanceTaskToResponse).collect(Collectors.toList());
    }

    // ==========================================
    // 3. Asset Requirement Templates
    // ==========================================

    @Transactional
    public AssetRequirementTemplateResponse addAssetRequirement(Long templateId, AssetRequirementTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name is required");
        }
        if (request.getAssetType() == null) {
            throw new IllegalArgumentException("Asset type is required");
        }
        if (request.getAssignedToType() == null) {
            throw new IllegalArgumentException("Assigned to type is required");
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        OffboardingAssetRequirementTemplate asset = new OffboardingAssetRequirementTemplate();
        asset.setOrganizationId(orgId);
        asset.setTemplateId(templateId);
        asset.setAssetType(request.getAssetType());
        asset.setName(request.getName().trim());
        asset.setDescription(request.getDescription());
        asset.setReturnRequired(request.getReturnRequired() != null ? request.getReturnRequired() : true);
        asset.setConditionCheckRequired(request.getConditionCheckRequired() != null ? request.getConditionCheckRequired() : true);
        asset.setSerialNumberVerificationRequired(request.getSerialNumberVerificationRequired() != null ? request.getSerialNumberVerificationRequired() : true);
        asset.setDueBeforeLwdDays(request.getDueBeforeLwdDays() != null ? request.getDueBeforeLwdDays() : 1);
        asset.setAssignedToType(request.getAssignedToType());
        asset.setAssignedUserId(request.getAssignedUserId());
        asset.setMandatory(request.getMandatory() != null ? request.getMandatory() : true);
        asset.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : false);
        asset.setSequence(request.getSequence() != null ? request.getSequence() : 1);
        asset.setActive(request.getActive() != null ? request.getActive() : true);

        asset = assetRequirementRepository.save(asset);
        return mapAssetReqToResponse(asset);
    }

    @Transactional
    public AssetRequirementTemplateResponse updateAssetRequirement(Long templateId, Long assetReqId, AssetRequirementTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingAssetRequirementTemplate asset = assetRequirementRepository.findByIdAndOrganizationId(assetReqId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset requirement template not found with ID: " + assetReqId));

        if (!asset.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Asset requirement does not belong to template with ID: " + templateId);
        }

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Asset name cannot be empty");
            }
            asset.setName(request.getName().trim());
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        if (request.getAssetType() != null) asset.setAssetType(request.getAssetType());
        if (request.getDescription() != null) asset.setDescription(request.getDescription());
        if (request.getReturnRequired() != null) asset.setReturnRequired(request.getReturnRequired());
        if (request.getConditionCheckRequired() != null) asset.setConditionCheckRequired(request.getConditionCheckRequired());
        if (request.getSerialNumberVerificationRequired() != null) asset.setSerialNumberVerificationRequired(request.getSerialNumberVerificationRequired());
        if (request.getDueBeforeLwdDays() != null) asset.setDueBeforeLwdDays(request.getDueBeforeLwdDays());
        if (request.getAssignedToType() != null) asset.setAssignedToType(request.getAssignedToType());
        if (request.getAssignedUserId() != null) asset.setAssignedUserId(request.getAssignedUserId());
        if (request.getMandatory() != null) asset.setMandatory(request.getMandatory());
        if (request.getApprovalRequired() != null) asset.setApprovalRequired(request.getApprovalRequired());
        if (request.getSequence() != null) asset.setSequence(request.getSequence());
        if (request.getActive() != null) asset.setActive(request.getActive());

        asset = assetRequirementRepository.save(asset);
        return mapAssetReqToResponse(asset);
    }

    @Transactional
    public void deleteAssetRequirement(Long templateId, Long assetReqId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingAssetRequirementTemplate asset = assetRequirementRepository.findByIdAndOrganizationId(assetReqId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset requirement template not found with ID: " + assetReqId));

        if (!asset.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Asset requirement does not belong to template with ID: " + templateId);
        }

        assetRequirementRepository.delete(asset);
    }

    public List<AssetRequirementTemplateResponse> listAssetRequirements(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);
        return assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId)
                .stream().map(this::mapAssetReqToResponse).collect(Collectors.toList());
    }

    // ==========================================
    // 4. Document Requirement Templates
    // ==========================================

    @Transactional
    public DocumentRequirementTemplateResponse addDocumentRequirement(Long templateId, DocumentRequirementTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        if (request.getDocumentName() == null || request.getDocumentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Document name is required");
        }
        if (request.getDocumentType() == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (request.getOwnerType() == null) {
            throw new IllegalArgumentException("Owner type is required");
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        OffboardingDocumentRequirementTemplate doc = new OffboardingDocumentRequirementTemplate();
        doc.setOrganizationId(orgId);
        doc.setTemplateId(templateId);
        doc.setDocumentType(request.getDocumentType());
        doc.setDocumentName(request.getDocumentName().trim());
        doc.setDescription(request.getDescription());
        doc.setRequired(request.getRequired() != null ? request.getRequired() : true);
        doc.setAction(request.getAction() != null ? request.getAction() : DocumentActionType.GENERATE);
        doc.setOwnerType(request.getOwnerType());
        doc.setOwnerUserId(request.getOwnerUserId());
        doc.setDueBeforeLwdDays(request.getDueBeforeLwdDays() != null ? request.getDueBeforeLwdDays() : 0);
        doc.setEmployeeUploadRequired(request.getEmployeeUploadRequired() != null ? request.getEmployeeUploadRequired() : false);
        doc.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : false);
        doc.setSequence(request.getSequence() != null ? request.getSequence() : 1);
        doc.setActive(request.getActive() != null ? request.getActive() : true);

        doc = documentRequirementRepository.save(doc);
        return mapDocReqToResponse(doc);
    }

    @Transactional
    public DocumentRequirementTemplateResponse updateDocumentRequirement(Long templateId, Long docReqId, DocumentRequirementTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingDocumentRequirementTemplate doc = documentRequirementRepository.findByIdAndOrganizationId(docReqId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Document requirement template not found with ID: " + docReqId));

        if (!doc.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Document requirement does not belong to template with ID: " + templateId);
        }

        if (request.getDocumentName() != null) {
            if (request.getDocumentName().trim().isEmpty()) {
                throw new IllegalArgumentException("Document name cannot be empty");
            }
            doc.setDocumentName(request.getDocumentName().trim());
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        if (request.getDocumentType() != null) doc.setDocumentType(request.getDocumentType());
        if (request.getDescription() != null) doc.setDescription(request.getDescription());
        if (request.getRequired() != null) doc.setRequired(request.getRequired());
        if (request.getAction() != null) doc.setAction(request.getAction());
        if (request.getOwnerType() != null) doc.setOwnerType(request.getOwnerType());
        if (request.getOwnerUserId() != null) doc.setOwnerUserId(request.getOwnerUserId());
        if (request.getDueBeforeLwdDays() != null) doc.setDueBeforeLwdDays(request.getDueBeforeLwdDays());
        if (request.getEmployeeUploadRequired() != null) doc.setEmployeeUploadRequired(request.getEmployeeUploadRequired());
        if (request.getApprovalRequired() != null) doc.setApprovalRequired(request.getApprovalRequired());
        if (request.getSequence() != null) doc.setSequence(request.getSequence());
        if (request.getActive() != null) doc.setActive(request.getActive());

        doc = documentRequirementRepository.save(doc);
        return mapDocReqToResponse(doc);
    }

    @Transactional
    public void deleteDocumentRequirement(Long templateId, Long docReqId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingDocumentRequirementTemplate doc = documentRequirementRepository.findByIdAndOrganizationId(docReqId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Document requirement template not found with ID: " + docReqId));

        if (!doc.getTemplateId().equals(templateId)) {
            throw new IllegalArgumentException("Document requirement does not belong to template with ID: " + templateId);
        }

        documentRequirementRepository.delete(doc);
    }

    public List<DocumentRequirementTemplateResponse> listDocumentRequirements(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);
        return documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(templateId, orgId)
                .stream().map(this::mapDocReqToResponse).collect(Collectors.toList());
    }

    // ==========================================
    // 5. Knowledge Transfer (KT) Templates
    // ==========================================

    @Transactional
    public KtTemplateResponse configureKtTemplate(Long templateId, KtTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("KT title is required");
        }
        if (request.getAssignToType() == null) {
            throw new IllegalArgumentException("Assign to type is required");
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }

        OffboardingKtTemplate kt = ktTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseGet(() -> {
                    OffboardingKtTemplate newKt = new OffboardingKtTemplate();
                    newKt.setOrganizationId(orgId);
                    newKt.setTemplateId(templateId);
                    return newKt;
                });

        kt.setRequired(request.getRequired() != null ? request.getRequired() : true);
        kt.setTitle(request.getTitle().trim());
        kt.setDescription(request.getDescription());
        kt.setAssignToType(request.getAssignToType());
        kt.setAssignedUserId(request.getAssignedUserId());
        kt.setEmployeeResponsibilitiesJson(writeListJson(request.getEmployeeResponsibilities()));
        kt.setHandoverDocumentRequired(request.getHandoverDocumentRequired() != null ? request.getHandoverDocumentRequired() : true);
        kt.setHandoverApprovalRequired(request.getHandoverApprovalRequired() != null ? request.getHandoverApprovalRequired() : true);
        kt.setDueBeforeLwdDays(request.getDueBeforeLwdDays() != null ? request.getDueBeforeLwdDays() : 5);
        kt.setPriority(request.getPriority() != null ? request.getPriority() : "HIGH");
        kt.setMandatory(request.getMandatory() != null ? request.getMandatory() : true);

        kt = ktTemplateRepository.save(kt);
        return mapKtToResponse(kt);
    }

    public KtTemplateResponse getKtTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);
        return ktTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .map(this::mapKtToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("KT template not configured for template ID: " + templateId));
    }

    @Transactional
    public void deleteKtTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);
        ktTemplateRepository.deleteByTemplateIdAndOrganizationId(templateId, orgId);
    }

    // ==========================================
    // 6. Exit Interview & Question Templates
    // ==========================================

    @Transactional
    public InterviewTemplateResponse configureInterviewTemplate(Long templateId, InterviewTemplateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        if (request.getConductedByType() == null) {
            throw new IllegalArgumentException("Conducted by type is required");
        }
        if (request.getDueBeforeLwdDays() != null && request.getDueBeforeLwdDays() < 0) {
            throw new IllegalArgumentException("Due before LWD days cannot be negative");
        }

        OffboardingInterviewTemplate interview = interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseGet(() -> {
                    OffboardingInterviewTemplate newInterview = new OffboardingInterviewTemplate();
                    newInterview.setOrganizationId(orgId);
                    newInterview.setTemplateId(templateId);
                    return newInterview;
                });

        interview.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        interview.setConductedByType(request.getConductedByType());
        interview.setConductedByUserId(request.getConductedByUserId());
        interview.setMandatory(request.getMandatory() != null ? request.getMandatory() : true);
        interview.setDueBeforeLwdDays(request.getDueBeforeLwdDays() != null ? request.getDueBeforeLwdDays() : 2);
        interview.setAllowAnonymousFeedback(request.getAllowAnonymousFeedback() != null ? request.getAllowAnonymousFeedback() : false);

        interview = interviewTemplateRepository.save(interview);
        Long interviewId = interview.getId();

        // Optional bulk question update if provided in request
        if (request.getQuestions() != null) {
            interviewQuestionRepository.deleteByInterviewTemplateIdAndOrganizationId(interviewId, orgId);
            int seq = 1;
            for (InterviewQuestionRequest qReq : request.getQuestions()) {
                if (qReq.getQuestion() == null || qReq.getQuestion().trim().isEmpty()) {
                    throw new IllegalArgumentException("Question text is required");
                }
                if (qReq.getQuestionType() == null) {
                    throw new IllegalArgumentException("Question type is required");
                }
                OffboardingInterviewQuestion q = new OffboardingInterviewQuestion();
                q.setOrganizationId(orgId);
                q.setInterviewTemplateId(interviewId);
                q.setQuestion(qReq.getQuestion().trim());
                q.setQuestionType(qReq.getQuestionType());
                q.setRequired(qReq.getRequired() != null ? qReq.getRequired() : true);
                q.setOptionsJson(writeListJson(qReq.getOptions()));
                q.setMinValue(qReq.getMinValue());
                q.setMaxValue(qReq.getMaxValue());
                q.setSequence(qReq.getSequence() != null ? qReq.getSequence() : seq++);
                q.setActive(qReq.getActive() != null ? qReq.getActive() : true);
                interviewQuestionRepository.save(q);
            }
        }

        InterviewTemplateResponse resp = mapInterviewToResponse(interview);
        List<OffboardingInterviewQuestion> questions = interviewQuestionRepository
                .findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(interviewId, orgId);
        resp.setQuestions(questions.stream().map(this::mapInterviewQuestionToResponse).collect(Collectors.toList()));
        return resp;
    }

    public InterviewTemplateResponse getInterviewTemplate(Long templateId) {
        Long orgId = TenantContext.requireOrganizationId();
        verifyTemplateExists(templateId, orgId);

        OffboardingInterviewTemplate interview = interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit interview template not configured for template ID: " + templateId));

        InterviewTemplateResponse resp = mapInterviewToResponse(interview);
        List<OffboardingInterviewQuestion> questions = interviewQuestionRepository
                .findByInterviewTemplateIdAndOrganizationIdOrderBySequenceAsc(interview.getId(), orgId);
        resp.setQuestions(questions.stream().map(this::mapInterviewQuestionToResponse).collect(Collectors.toList()));
        return resp;
    }

    @Transactional
    public InterviewQuestionResponse addInterviewQuestion(Long templateId, InterviewQuestionRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingInterviewTemplate interview = interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit interview template not configured for template ID: " + templateId));

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("Question text is required");
        }
        if (request.getQuestionType() == null) {
            throw new IllegalArgumentException("Question type is required");
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        OffboardingInterviewQuestion q = new OffboardingInterviewQuestion();
        q.setOrganizationId(orgId);
        q.setInterviewTemplateId(interview.getId());
        q.setQuestion(request.getQuestion().trim());
        q.setQuestionType(request.getQuestionType());
        q.setRequired(request.getRequired() != null ? request.getRequired() : true);
        q.setOptionsJson(writeListJson(request.getOptions()));
        q.setMinValue(request.getMinValue());
        q.setMaxValue(request.getMaxValue());
        q.setSequence(request.getSequence() != null ? request.getSequence() : 1);
        q.setActive(request.getActive() != null ? request.getActive() : true);

        q = interviewQuestionRepository.save(q);
        return mapInterviewQuestionToResponse(q);
    }

    @Transactional
    public InterviewQuestionResponse updateInterviewQuestion(Long templateId, Long questionId, InterviewQuestionRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingInterviewTemplate interview = interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit interview template not configured for template ID: " + templateId));

        OffboardingInterviewQuestion q = interviewQuestionRepository.findByIdAndOrganizationId(questionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found with ID: " + questionId));

        if (!q.getInterviewTemplateId().equals(interview.getId())) {
            throw new IllegalArgumentException("Question does not belong to template with ID: " + templateId);
        }

        if (request.getQuestion() != null) {
            if (request.getQuestion().trim().isEmpty()) {
                throw new IllegalArgumentException("Question text cannot be empty");
            }
            q.setQuestion(request.getQuestion().trim());
        }
        if (request.getSequence() != null && request.getSequence() < 1) {
            throw new IllegalArgumentException("Sequence must be greater than or equal to 1");
        }

        if (request.getQuestionType() != null) q.setQuestionType(request.getQuestionType());
        if (request.getRequired() != null) q.setRequired(request.getRequired());
        if (request.getOptions() != null) q.setOptionsJson(writeListJson(request.getOptions()));
        if (request.getMinValue() != null) q.setMinValue(request.getMinValue());
        if (request.getMaxValue() != null) q.setMaxValue(request.getMaxValue());
        if (request.getSequence() != null) q.setSequence(request.getSequence());
        if (request.getActive() != null) q.setActive(request.getActive());

        q = interviewQuestionRepository.save(q);
        return mapInterviewQuestionToResponse(q);
    }

    @Transactional
    public void deleteInterviewQuestion(Long templateId, Long questionId) {
        Long orgId = TenantContext.requireOrganizationId();
        OffboardingInterviewTemplate interview = interviewTemplateRepository.findByTemplateIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit interview template not configured for template ID: " + templateId));

        OffboardingInterviewQuestion q = interviewQuestionRepository.findByIdAndOrganizationId(questionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found with ID: " + questionId));

        if (!q.getInterviewTemplateId().equals(interview.getId())) {
            throw new IllegalArgumentException("Question does not belong to interview template for template ID: " + templateId);
        }

        interviewQuestionRepository.delete(q);
    }

    // ==========================================
    // Helper Methods & Mappings
    // ==========================================

    private void verifyTemplateExists(Long templateId, Long orgId) {
        if (!templateRepository.findByIdAndOrganizationId(templateId, orgId).isPresent()) {
            throw new ResourceNotFoundException("Offboarding template not found with ID: " + templateId);
        }
    }

    private OffboardingTemplateResponse mapToResponse(OffboardingTemplate t) {
        OffboardingTemplateResponse resp = new OffboardingTemplateResponse();
        resp.setId(t.getId());
        resp.setOrganizationId(t.getOrganizationId());
        resp.setName(t.getName());
        resp.setDescription(t.getDescription());
        resp.setStatus(t.getStatus());
        resp.setDepartmentIds(readListJson(t.getDepartmentIdsJson()));
        resp.setEmploymentTypes(readListJson(t.getEmploymentTypesJson()));
        resp.setEmployeeTypes(readListJson(t.getEmployeeTypesJson()));
        resp.setNoticePeriodEnabled(t.getNoticePeriodEnabled());
        resp.setNoticePeriodDefaultDays(t.getNoticePeriodDefaultDays());
        resp.setAllowEarlyRelease(t.getAllowEarlyRelease());
        resp.setAllowNoticePeriodBuyout(t.getAllowNoticePeriodBuyout());
        resp.setCreatedAt(t.getCreatedAt());
        resp.setUpdatedAt(t.getUpdatedAt());

        // Quick count lookups
        resp.setClearanceTaskCount(clearanceTaskRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(t.getId(), t.getOrganizationId()).size());
        resp.setAssetRequirementCount(assetRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(t.getId(), t.getOrganizationId()).size());
        resp.setDocumentRequirementCount(documentRequirementRepository.findByTemplateIdAndOrganizationIdOrderBySequenceAsc(t.getId(), t.getOrganizationId()).size());
        resp.setKtConfigured(ktTemplateRepository.findByTemplateIdAndOrganizationId(t.getId(), t.getOrganizationId()).isPresent());
        resp.setInterviewConfigured(interviewTemplateRepository.findByTemplateIdAndOrganizationId(t.getId(), t.getOrganizationId()).isPresent());

        return resp;
    }

    private ClearanceTaskTemplateResponse mapClearanceTaskToResponse(OffboardingClearanceTaskTemplate t) {
        ClearanceTaskTemplateResponse resp = new ClearanceTaskTemplateResponse();
        resp.setId(t.getId());
        resp.setOrganizationId(t.getOrganizationId());
        resp.setTemplateId(t.getTemplateId());
        resp.setTaskName(t.getTaskName());
        resp.setDescription(t.getDescription());
        resp.setAssignToType(t.getAssignToType());
        resp.setAssignedUserId(t.getAssignedUserId());
        resp.setDueBeforeLwdDays(t.getDueBeforeLwdDays());
        resp.setPriority(t.getPriority());
        resp.setMandatory(t.getMandatory());
        resp.setApprovalRequired(t.getApprovalRequired());
        resp.setSequence(t.getSequence());
        resp.setActive(t.getActive());
        resp.setCreatedAt(t.getCreatedAt());
        resp.setUpdatedAt(t.getUpdatedAt());
        return resp;
    }

    private AssetRequirementTemplateResponse mapAssetReqToResponse(OffboardingAssetRequirementTemplate a) {
        AssetRequirementTemplateResponse resp = new AssetRequirementTemplateResponse();
        resp.setId(a.getId());
        resp.setOrganizationId(a.getOrganizationId());
        resp.setTemplateId(a.getTemplateId());
        resp.setAssetType(a.getAssetType());
        resp.setName(a.getName());
        resp.setDescription(a.getDescription());
        resp.setReturnRequired(a.getReturnRequired());
        resp.setConditionCheckRequired(a.getConditionCheckRequired());
        resp.setSerialNumberVerificationRequired(a.getSerialNumberVerificationRequired());
        resp.setDueBeforeLwdDays(a.getDueBeforeLwdDays());
        resp.setAssignedToType(a.getAssignedToType());
        resp.setAssignedUserId(a.getAssignedUserId());
        resp.setMandatory(a.getMandatory());
        resp.setApprovalRequired(a.getApprovalRequired());
        resp.setSequence(a.getSequence());
        resp.setActive(a.getActive());
        resp.setCreatedAt(a.getCreatedAt());
        resp.setUpdatedAt(a.getUpdatedAt());
        return resp;
    }

    private DocumentRequirementTemplateResponse mapDocReqToResponse(OffboardingDocumentRequirementTemplate d) {
        DocumentRequirementTemplateResponse resp = new DocumentRequirementTemplateResponse();
        resp.setId(d.getId());
        resp.setOrganizationId(d.getOrganizationId());
        resp.setTemplateId(d.getTemplateId());
        resp.setDocumentType(d.getDocumentType());
        resp.setDocumentName(d.getDocumentName());
        resp.setDescription(d.getDescription());
        resp.setRequired(d.getRequired());
        resp.setAction(d.getAction());
        resp.setOwnerType(d.getOwnerType());
        resp.setOwnerUserId(d.getOwnerUserId());
        resp.setDueBeforeLwdDays(d.getDueBeforeLwdDays());
        resp.setEmployeeUploadRequired(d.getEmployeeUploadRequired());
        resp.setApprovalRequired(d.getApprovalRequired());
        resp.setSequence(d.getSequence());
        resp.setActive(d.getActive());
        resp.setCreatedAt(d.getCreatedAt());
        resp.setUpdatedAt(d.getUpdatedAt());
        return resp;
    }

    private KtTemplateResponse mapKtToResponse(OffboardingKtTemplate k) {
        KtTemplateResponse resp = new KtTemplateResponse();
        resp.setId(k.getId());
        resp.setOrganizationId(k.getOrganizationId());
        resp.setTemplateId(k.getTemplateId());
        resp.setRequired(k.getRequired());
        resp.setTitle(k.getTitle());
        resp.setDescription(k.getDescription());
        resp.setAssignToType(k.getAssignToType());
        resp.setAssignedUserId(k.getAssignedUserId());
        resp.setEmployeeResponsibilities(readListJson(k.getEmployeeResponsibilitiesJson()));
        resp.setHandoverDocumentRequired(k.getHandoverDocumentRequired());
        resp.setHandoverApprovalRequired(k.getHandoverApprovalRequired());
        resp.setDueBeforeLwdDays(k.getDueBeforeLwdDays());
        resp.setPriority(k.getPriority());
        resp.setMandatory(k.getMandatory());
        resp.setCreatedAt(k.getCreatedAt());
        resp.setUpdatedAt(k.getUpdatedAt());
        return resp;
    }

    private InterviewTemplateResponse mapInterviewToResponse(OffboardingInterviewTemplate i) {
        InterviewTemplateResponse resp = new InterviewTemplateResponse();
        resp.setId(i.getId());
        resp.setOrganizationId(i.getOrganizationId());
        resp.setTemplateId(i.getTemplateId());
        resp.setEnabled(i.getEnabled());
        resp.setConductedByType(i.getConductedByType());
        resp.setConductedByUserId(i.getConductedByUserId());
        resp.setMandatory(i.getMandatory());
        resp.setDueBeforeLwdDays(i.getDueBeforeLwdDays());
        resp.setAllowAnonymousFeedback(i.getAllowAnonymousFeedback());
        resp.setCreatedAt(i.getCreatedAt());
        resp.setUpdatedAt(i.getUpdatedAt());
        return resp;
    }

    private InterviewQuestionResponse mapInterviewQuestionToResponse(OffboardingInterviewQuestion q) {
        InterviewQuestionResponse resp = new InterviewQuestionResponse();
        resp.setId(q.getId());
        resp.setOrganizationId(q.getOrganizationId());
        resp.setInterviewTemplateId(q.getInterviewTemplateId());
        resp.setQuestion(q.getQuestion());
        resp.setQuestionType(q.getQuestionType());
        resp.setRequired(q.getRequired());
        resp.setOptions(readListJson(q.getOptionsJson()));
        resp.setMinValue(q.getMinValue());
        resp.setMaxValue(q.getMaxValue());
        resp.setSequence(q.getSequence());
        resp.setActive(q.getActive());
        resp.setCreatedAt(q.getCreatedAt());
        resp.setUpdatedAt(q.getUpdatedAt());
        return resp;
    }

    private String writeListJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readListJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
