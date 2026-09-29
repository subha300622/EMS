package com.example.ems.offboarding.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentRequest;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentResponse;
import com.example.ems.offboarding.entity.OffboardingEmployeeTemplateAssignment;
import com.example.ems.offboarding.entity.OffboardingTemplate;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import com.example.ems.offboarding.repository.OffboardingEmployeeTemplateAssignmentRepository;
import com.example.ems.offboarding.repository.OffboardingTemplateRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OffboardingTemplateAssignmentService {

    private final OffboardingEmployeeTemplateAssignmentRepository assignmentRepository;
    private final OffboardingTemplateRepository templateRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffboardingTemplateAssignmentService(
            OffboardingEmployeeTemplateAssignmentRepository assignmentRepository,
            OffboardingTemplateRepository templateRepository,
            EmployeeRepository employeeRepository,
            ObjectMapper objectMapper) {
        this.assignmentRepository = assignmentRepository;
        this.templateRepository = templateRepository;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // 1. Assign / Override Template for Individual Employee
    // =========================================================================
    @Transactional
    public EmployeeTemplateAssignmentResponse assignTemplateToEmployee(Long employeeId, EmployeeTemplateAssignmentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getOrganization() == null || !orgId.equals(employee.getOrganization().getId())) {
            throw new ResourceNotFoundException("Employee not found in organization ID: " + orgId);
        }

        OffboardingTemplate template = templateRepository.findByIdAndOrganizationId(request.getTemplateId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        if (template.getStatus() != OffboardingTemplateStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign an INACTIVE offboarding template (Template ID: " + template.getId() + ")");
        }

        String exitType = (request.getExitType() != null && !request.getExitType().trim().isEmpty())
                ? request.getExitType().trim().toUpperCase()
                : "ALL";

        OffboardingEmployeeTemplateAssignment assignment = assignmentRepository
                .findByOrganizationIdAndEmployeeIdAndExitType(orgId, employeeId, exitType)
                .orElseGet(() -> new OffboardingEmployeeTemplateAssignment(orgId, employee, template, exitType));

        assignment.setTemplate(template);
        assignment.setExitType(exitType);

        OffboardingEmployeeTemplateAssignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    // =========================================================================
    // 2. Remove Individual Assignment (Revert to Auto-Matching)
    // =========================================================================
    @Transactional
    public void removeAssignment(Long employeeId, String exitType) {
        Long orgId = TenantContext.requireOrganizationId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getOrganization() == null || !orgId.equals(employee.getOrganization().getId())) {
            throw new ResourceNotFoundException("Employee not found in organization ID: " + orgId);
        }

        if (exitType != null && !exitType.trim().isEmpty()) {
            assignmentRepository.deleteByOrganizationIdAndEmployeeIdAndExitType(orgId, employeeId, exitType.trim().toUpperCase());
        } else {
            assignmentRepository.deleteByOrganizationIdAndEmployeeId(orgId, employeeId);
        }
    }

    // =========================================================================
    // 3. View Current Individual Assignments
    // =========================================================================
    @Transactional(readOnly = true)
    public List<EmployeeTemplateAssignmentResponse> getAssignments(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getOrganization() == null || !orgId.equals(employee.getOrganization().getId())) {
            throw new ResourceNotFoundException("Employee not found in organization ID: " + orgId);
        }

        return assignmentRepository.findByOrganizationIdAndEmployeeId(orgId, employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 4. Resolve Template for Employee (Multi-Tier Decision Tree)
    // =========================================================================
    @Transactional(readOnly = true)
    public OffboardingTemplate resolveTemplateForEmployee(Employee employee, String exitType) {
        Long orgId = employee.getOrganization() != null
                ? employee.getOrganization().getId()
                : TenantContext.requireOrganizationId();

        String normalizedExitType = (exitType != null && !exitType.trim().isEmpty())
                ? exitType.trim().toUpperCase()
                : "RESIGNATION";

        // Tier 1: Check Individual Assignment (Exact Exit Type or 'ALL')
        Optional<OffboardingEmployeeTemplateAssignment> directAssign = assignmentRepository
                .findByOrganizationIdAndEmployeeIdAndExitType(orgId, employee.getId(), normalizedExitType);

        if (directAssign.isEmpty()) {
            directAssign = assignmentRepository.findByOrganizationIdAndEmployeeIdAndExitType(orgId, employee.getId(), "ALL");
        }

        if (directAssign.isPresent()) {
            OffboardingTemplate assignedTemplate = directAssign.get().getTemplate();
            if (assignedTemplate != null && assignedTemplate.getStatus() == OffboardingTemplateStatus.ACTIVE) {
                return assignedTemplate;
            }
        }

        // Tier 2: Automatic Specificity Matching
        List<OffboardingTemplate> activeTemplates = templateRepository.findByOrganizationIdAndStatus(orgId, OffboardingTemplateStatus.ACTIVE);

        if (activeTemplates.isEmpty()) {
            throw new ResourceNotFoundException("No active offboarding template found for organization ID: " + orgId);
        }

        OffboardingTemplate bestTemplate = null;
        int highestScore = -1;

        for (OffboardingTemplate template : activeTemplates) {
            // Hard matching check: all specified non-empty criteria MUST match
            boolean deptMatches = matchesCriterion(template.getDepartmentIdsJson(), employee.getDepartment());
            boolean empTypeMatches = matchesCriterion(template.getEmploymentTypesJson(), employee.getEmploymentType());
            boolean designationMatches = matchesCriterion(template.getEmployeeTypesJson(), employee.getDesignation());

            if (deptMatches && empTypeMatches && designationMatches) {
                int score = 0;
                if (isSpecific(template.getDepartmentIdsJson())) score += 100;
                if (isSpecific(template.getEmploymentTypesJson())) score += 50;
                if (isSpecific(template.getEmployeeTypesJson())) score += 25;

                if (score > highestScore) {
                    highestScore = score;
                    bestTemplate = template;
                }
            }
        }

        if (bestTemplate != null) {
            return bestTemplate;
        }

        // Tier 3: Global Fallback (Return the first active template)
        return activeTemplates.get(0);
    }

    private boolean matchesCriterion(String jsonCriterion, String employeeValue) {
        if (jsonCriterion == null || jsonCriterion.trim().isEmpty() || jsonCriterion.trim().equals("[]") || jsonCriterion.trim().equalsIgnoreCase("null")) {
            return true; // Wildcard match
        }
        if (employeeValue == null || employeeValue.trim().isEmpty()) {
            return false;
        }
        try {
            List<String> list = objectMapper.readValue(jsonCriterion, new TypeReference<List<String>>() {});
            if (list == null || list.isEmpty()) {
                return true;
            }
            return list.stream().anyMatch(val -> val != null && val.trim().equalsIgnoreCase(employeeValue.trim()));
        } catch (JsonProcessingException e) {
            String raw = jsonCriterion.replace("[", "").replace("]", "").replace("\"", "");
            String[] tokens = raw.split(",");
            for (String t : tokens) {
                if (t.trim().equalsIgnoreCase(employeeValue.trim())) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isSpecific(String jsonCriterion) {
        if (jsonCriterion == null || jsonCriterion.trim().isEmpty() || jsonCriterion.trim().equals("[]") || jsonCriterion.trim().equalsIgnoreCase("null")) {
            return false;
        }
        try {
            List<String> list = objectMapper.readValue(jsonCriterion, new TypeReference<List<String>>() {});
            return list != null && !list.isEmpty();
        } catch (JsonProcessingException e) {
            return true;
        }
    }

    private EmployeeTemplateAssignmentResponse mapToResponse(OffboardingEmployeeTemplateAssignment assignment) {
        Employee emp = assignment.getEmployee();
        OffboardingTemplate tpl = assignment.getTemplate();

        return new EmployeeTemplateAssignmentResponse(
                assignment.getId(),
                emp != null ? emp.getId() : null,
                emp != null ? emp.getFullName() : null,
                emp != null ? emp.getEmployeeId() : null,
                tpl != null ? tpl.getId() : null,
                tpl != null ? tpl.getName() : null,
                assignment.getExitType(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
