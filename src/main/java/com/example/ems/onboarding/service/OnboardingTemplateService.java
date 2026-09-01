package com.example.ems.onboarding.service;

import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.onboarding.dto.OnboardingTemplateCreateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateUpdateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateDuplicateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateResponse;
import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OnboardingTemplateService {

    @Autowired
    private OnboardingTemplateRepository templateRepository;

    @Autowired
    private com.example.ems.onboarding.repository.OnboardingRepository onboardingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public OnboardingTemplateResponse createTemplate(OnboardingTemplateCreateRequest request) {
        // 1. Business Validations
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo date must be after or equal to effectiveFrom date");
        }

        // Validate sections
        if (request.getSections() == null || request.getSections().isEmpty()) {
            throw new IllegalArgumentException("sections must contain at least one section");
        }

        for (OnboardingTemplateCreateRequest.SectionRequest sec : request.getSections()) {
            if (sec.getName() == null || sec.getName().isBlank()) {
                throw new IllegalArgumentException("Section name must not be blank");
            }
            if (sec.getTasks() == null || sec.getTasks().isEmpty()) {
                throw new IllegalArgumentException("Section must contain at least one task");
            }
            for (OnboardingTemplateCreateRequest.TaskRequest task : sec.getTasks()) {
                if (task.getName() == null || task.getName().isBlank()) {
                    throw new IllegalArgumentException("Task name must not be blank");
                }
                if (task.getDueDays() < 0) {
                    throw new IllegalArgumentException("dueDays must be >= 0");
                }
            }
        }

        // Validate documents
        if (request.getDocuments() != null) {
            for (OnboardingTemplateCreateRequest.DocumentRequest doc : request.getDocuments()) {
                if (doc.getName() == null || doc.getName().isBlank()) {
                    throw new IllegalArgumentException("Document name must not be blank");
                }
                if (doc.getMaxSize() <= 0) {
                    throw new IllegalArgumentException("maxSize must be greater than 0");
                }
            }
        }

        // 2. Department Lookup
        Department department;
        if (request.getDepartmentId().matches("\\d+")) {
            department = departmentRepository.findById(Long.parseLong(request.getDepartmentId()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Department not found with ID: " + request.getDepartmentId()));
        } else {
            department = departmentRepository.findByCode(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Department not found with Code: " + request.getDepartmentId()));
        }

        // 3. Reset existing default template if isDefault is true
        if (request.getIsDefault()) {
            templateRepository.resetDefaultTemplate(
                    request.getDepartmentId(),
                    request.getDesignation(),
                    request.getEmploymentType());
        }

        // 4. Generate unique templateCode
        String deptCode = department.getCode();
        if (deptCode.contains("-")) {
            deptCode = deptCode.split("-")[0];
        }
        long seq = templateRepository.countByDepartmentId(request.getDepartmentId()) + 1;
        String templateCode = "TPL-" + deptCode.toUpperCase() + "-" + String.format("%03d", seq);

        // 5. Build and save OnboardingTemplate entity
        OnboardingTemplate template = new OnboardingTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDepartmentId(request.getDepartmentId());
        template.setDesignation(request.getDesignation());
        template.setEmploymentType(request.getEmploymentType());
        template.setExperienceLevel(request.getExperienceLevel());
        template.setEffectiveFrom(request.getEffectiveFrom());
        template.setEffectiveTo(request.getEffectiveTo());
        template.setIsDefault(request.getIsDefault());
        template.setStatus("ACTIVE");
        template.setVersion(1);
        template.setTemplateCode(templateCode);

        try {
            template.setSectionsJson(objectMapper.writeValueAsString(request.getSections()));
            template.setDocumentsJson(objectMapper.writeValueAsString(
                    request.getDocuments() != null ? request.getDocuments() : Collections.emptyList()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize template JSON structure: " + e.getMessage(), e);
        }

        template = templateRepository.save(template);

        // 6. Return response
        return mapToResponse(template);
    }

    public java.util.Map<String, Object> getTemplatesList(String department, String status, int page, int limit,
            String search) {
        List<OnboardingTemplate> all = templateRepository.findAll();

        String targetDeptId = null;
        if (department != null && !department.isBlank()) {
            Optional<Department> deptOpt = departmentRepository.findByNameIgnoreCase(department);
            if (deptOpt.isPresent()) {
                targetDeptId = deptOpt.get().getCode();
            } else {
                targetDeptId = department;
            }
        }

        final String finalDeptId = targetDeptId;

        java.util.stream.Stream<OnboardingTemplate> stream = all.stream();

        if (finalDeptId != null) {
            stream = stream.filter(t -> finalDeptId.equalsIgnoreCase(t.getDepartmentId()));
        }
        if (status != null && !status.isBlank()) {
            stream = stream.filter(t -> status.equalsIgnoreCase(t.getStatus()));
        }
        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase();
            stream = stream.filter(t -> (t.getName() != null && t.getName().toLowerCase().contains(lowerSearch)) ||
                    (t.getDescription() != null && t.getDescription().toLowerCase().contains(lowerSearch)));
        }

        List<OnboardingTemplate> filtered = stream.collect(java.util.stream.Collectors.toList());

        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        if (totalPages == 0)
            totalPages = 1;

        int fromIndex = (page - 1) * limit;
        List<OnboardingTemplate> pageItems = Collections.emptyList();
        if (fromIndex < total) {
            int toIndex = Math.min(fromIndex + limit, total);
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        List<OnboardingTemplateResponse> items = pageItems.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("items", items);

        java.util.Map<String, Object> pagination = new java.util.HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", total);
        pagination.put("totalPages", totalPages);
        data.put("pagination", pagination);

        return data;
    }

    public OnboardingTemplateResponse getTemplateDetails(String templateId) {
        OnboardingTemplate template = templateRepository.findByTemplateCode(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        return mapToResponse(template);
    }

    @Transactional
    public OnboardingTemplateResponse updateTemplate(String templateId, OnboardingTemplateUpdateRequest request) {
        OnboardingTemplate template = templateRepository.findByTemplateCode(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));

        if (request.getName() != null)
            template.setName(request.getName());
        if (request.getDescription() != null)
            template.setDescription(request.getDescription());
        if (request.getStatus() != null)
            template.setStatus(request.getStatus().toUpperCase());

        if (request.getIsDefault() != null) {
            template.setIsDefault(request.getIsDefault());
            if (request.getIsDefault()) {
                templateRepository.resetDefaultTemplate(
                        template.getDepartmentId(),
                        template.getDesignation(),
                        template.getEmploymentType());
            }
        }

        if (request.getSections() != null) {
            if (request.getSections().isEmpty()) {
                throw new IllegalArgumentException("sections must contain at least one section");
            }
            try {
                template.setSectionsJson(objectMapper.writeValueAsString(request.getSections()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize sections: " + e.getMessage());
            }
        }

        if (request.getDocuments() != null) {
            try {
                template.setDocumentsJson(objectMapper.writeValueAsString(request.getDocuments()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize documents: " + e.getMessage());
            }
        }

        template.setVersion(template.getVersion() + 1);

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Transactional
    public OnboardingTemplateResponse duplicateTemplate(String templateId, OnboardingTemplateDuplicateRequest request) {
        OnboardingTemplate template = templateRepository.findByTemplateCode(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));

        OnboardingTemplate copy = new OnboardingTemplate();
        copy.setName(request.getName() != null ? request.getName() : template.getName() + " (Copy)");
        copy.setDescription(template.getDescription());
        copy.setDepartmentId(template.getDepartmentId());
        copy.setDesignation(template.getDesignation());
        copy.setEmploymentType(template.getEmploymentType());
        copy.setExperienceLevel(template.getExperienceLevel());
        copy.setEffectiveFrom(template.getEffectiveFrom());
        copy.setEffectiveTo(template.getEffectiveTo());
        copy.setIsDefault(false);
        copy.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT");
        copy.setVersion(1);
        copy.setUsageCount(0);
        copy.setSectionsJson(template.getSectionsJson());
        copy.setDocumentsJson(template.getDocumentsJson());

        Department dept = null;
        if (template.getDepartmentId().matches("\\d+")) {
            dept = departmentRepository.findById(Long.parseLong(template.getDepartmentId())).orElse(null);
        } else {
            dept = departmentRepository.findByCode(template.getDepartmentId()).orElse(null);
        }
        String deptCode = dept != null ? dept.getCode() : "DEPT";
        if (deptCode.contains("-")) {
            deptCode = deptCode.split("-")[0];
        }
        long seq = templateRepository.countByDepartmentId(template.getDepartmentId()) + 1;
        String newCode = "TPL-" + deptCode.toUpperCase() + "-" + String.format("%03d", seq);
        copy.setTemplateCode(newCode);

        copy = templateRepository.save(copy);
        return mapToResponse(copy);
    }

    @Transactional
    public java.util.Map<String, Object> deleteTemplate(String templateId) {
        OnboardingTemplate template = templateRepository.findByTemplateCode(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", templateId);

        boolean isInUse = template.getUsageCount() > 0
                || onboardingRepository.existsByAssignedTemplateId(templateId);

        if (!isInUse) {
            templateRepository.delete(template);
            result.put("status", "deleted");
        } else {
            template.setStatus("ARCHIVED");
            templateRepository.save(template);
            result.put("status", "archived");
        }
        return result;
    }

    public OnboardingTemplateResponse mapToResponse(OnboardingTemplate template) {
        OnboardingTemplateResponse resp = new OnboardingTemplateResponse();
        resp.setId(template.getTemplateCode());
        resp.setTemplateCode(template.getTemplateCode());
        resp.setName(template.getName());
        resp.setDescription(template.getDescription());
        resp.setDepartmentId(template.getDepartmentId());

        Department dept = null;
        if (template.getDepartmentId() != null) {
            if (template.getDepartmentId().matches("\\d+")) {
                dept = departmentRepository.findById(Long.parseLong(template.getDepartmentId())).orElse(null);
            } else {
                dept = departmentRepository.findByCode(template.getDepartmentId()).orElse(null);
            }
        }
        resp.setDept(dept != null ? dept.getName() : template.getDepartmentId());
        resp.setDeptColor("#00B87C");

        resp.setUsageCount(template.getUsageCount());
        resp.setStatus(template.getStatus().toLowerCase());
        resp.setVersion(template.getVersion());
        resp.setDesignation(template.getDesignation());
        resp.setEmploymentType(template.getEmploymentType());
        resp.setExperienceLevel(template.getExperienceLevel());
        resp.setEffectiveFrom(template.getEffectiveFrom());
        resp.setEffectiveTo(template.getEffectiveTo());
        resp.setIsDefault(template.getIsDefault());

        List<OnboardingTemplateCreateRequest.SectionRequest> sections = Collections.emptyList();
        List<OnboardingTemplateCreateRequest.DocumentRequest> documents = Collections.emptyList();
        try {
            if (template.getSectionsJson() != null) {
                sections = objectMapper.readValue(template.getSectionsJson(),
                        new TypeReference<List<OnboardingTemplateCreateRequest.SectionRequest>>() {
                        });
            }
            if (template.getDocumentsJson() != null) {
                documents = objectMapper.readValue(template.getDocumentsJson(),
                        new TypeReference<List<OnboardingTemplateCreateRequest.DocumentRequest>>() {
                        });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse template JSON structure: " + e.getMessage(), e);
        }
        resp.setSections(sections);
        resp.setDocuments(documents);

        resp.setPhases(sections.size());

        int totalTasks = sections.stream()
                .mapToInt(s -> s.getTasks() != null ? s.getTasks().size() : 0)
                .sum();
        resp.setTasks(totalTasks);

        int maxDueDays = sections.stream()
                .flatMap(s -> s.getTasks() != null ? s.getTasks().stream() : java.util.stream.Stream.empty())
                .mapToInt(OnboardingTemplateCreateRequest.TaskRequest::getDueDays)
                .max()
                .orElse(0);
        resp.setAvgDays(maxDueDays + " days");

        return resp;
    }
}
