package com.example.ems.offboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.ClearanceActionRequest;
import com.example.ems.offboarding.dto.ClearanceAssignmentRequest;
import com.example.ems.offboarding.dto.ExitClearanceDto;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExitClearanceService {

    @Autowired
    private ExitClearanceRepository clearanceRepository;

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

    private static final String DEPT_IT = "IT";
    private static final String DEPT_ADMIN = "ADMIN";
    private static final String DEPT_FINANCE = "FINANCE";
    private static final String DEPT_MANAGER = "MANAGER";

    private static final String REASON_IT = "Verify company IT assets, recover assigned devices and revoke all system/application access.";
    private static final String REASON_ADMIN = "Verify return of ID card, access card, keys and other company property.";
    private static final String REASON_FINANCE = "Verify employee expenses, advances, loans and outstanding financial obligations.";
    private static final String REASON_MANAGER = "Verify knowledge transfer, project handover, pending tasks and transfer of responsibilities.";

    @Transactional
    public List<ExitClearance> initializeClearancesForExit(EmployeeExit exit) {
        bindRls();
        Long orgId = exit.getOrganization().getId();
        List<ExitClearance> created = new ArrayList<>();

        // 1. Resolve assignees
        Employee itAssignee = resolveDepartmentAssignee(orgId, "IT");
        Employee adminAssignee = resolveDepartmentAssignee(orgId, "Administration");
        Employee financeAssignee = resolveDepartmentAssignee(orgId, "Finance");
        Employee managerAssignee = exit.getReportingManager() != null ? exit.getReportingManager() : resolveFallbackAssignee(orgId);

        createIfNotExists(exit, DEPT_IT, itAssignee, REASON_IT, created);
        createIfNotExists(exit, DEPT_ADMIN, adminAssignee, REASON_ADMIN, created);
        createIfNotExists(exit, DEPT_FINANCE, financeAssignee, REASON_FINANCE, created);
        createIfNotExists(exit, DEPT_MANAGER, managerAssignee, REASON_MANAGER, created);

        return created;
    }

    private void createIfNotExists(EmployeeExit exit, String department, Employee assignee, String reason, List<ExitClearance> list) {
        Optional<ExitClearance> existing = clearanceRepository.findByExitIdAndDepartment(exit.getId(), department);
        if (existing.isEmpty()) {
            ExitClearance c = new ExitClearance();
            c.setOrganization(exit.getOrganization());
            c.setExit(exit);
            c.setDepartment(department);
            c.setAssignedTo(assignee);
            c.setClearanceReason(reason);
            c.setStatus("PENDING");
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            list.add(clearanceRepository.save(c));
        } else {
            list.add(existing.get());
        }
    }

    private Employee resolveDepartmentAssignee(Long orgId, String deptName) {
        List<Employee> emps = employeeRepository.findByOrganizationIdAndDepartment(orgId, deptName);
        if (emps != null && !emps.isEmpty()) {
            return emps.get(0);
        }
        return resolveFallbackAssignee(orgId);
    }

    private Employee resolveFallbackAssignee(Long orgId) {
        List<Employee> actives = employeeRepository.findByOrganizationIdAndStatus(orgId, "ACTIVE");
        if (!actives.isEmpty()) {
            return actives.get(0);
        }
        List<Employee> all = employeeRepository.findByOrganizationId(orgId);
        if (!all.isEmpty()) {
            return all.get(0);
        }
        throw new IllegalStateException("No active employees found in organization to assign clearance task.");
    }

    @Transactional(readOnly = true)
    public List<ExitClearanceDto> getClearancesForExit(User currentUser, Long exitId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        EmployeeExit exit = exitRepository.findByIdAndOrganizationId(exitId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee exit not found with ID: " + exitId));

        return clearanceRepository.findByExitIdAndOrganizationId(exit.getId(), orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExitClearanceDto updateClearanceAssignment(User currentUser, Long exitId, Long clearanceId, ClearanceAssignmentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();

        ExitClearance clearance = clearanceRepository.findByIdAndOrganizationId(clearanceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Clearance record not found with ID: " + clearanceId));

        if (!clearance.getExit().getId().equals(exitId)) {
            throw new IllegalArgumentException("Clearance record " + clearanceId + " does not belong to exit " + exitId);
        }

        // Validate assigned user belongs to tenant & is active
        Employee newAssignee = employeeRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee employee not found with ID: " + request.getAssignedToUserId()));

        if (newAssignee.getOrganization() == null || !orgId.equals(newAssignee.getOrganization().getId())) {
            throw new IllegalArgumentException("Assignee employee does not belong to the organization");
        }

        if (!"ACTIVE".equalsIgnoreCase(newAssignee.getStatus())) {
            throw new IllegalArgumentException("Cannot assign clearance to an inactive employee");
        }

        clearance.setAssignedTo(newAssignee);
        if (request.getClearanceReason() != null && !request.getClearanceReason().trim().isEmpty()) {
            clearance.setClearanceReason(request.getClearanceReason().trim());
        }
        if (request.getRemarks() != null) {
            clearance.setRemarks(request.getRemarks());
        }
        clearance.setUpdatedAt(LocalDateTime.now());

        ExitClearance saved = clearanceRepository.save(clearance);
        return mapToDto(saved);
    }

    @Transactional
    public ExitClearanceDto processClearanceAction(User currentUser, Long exitId, Long clearanceId, ClearanceActionRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();

        ExitClearance clearance = clearanceRepository.findByIdAndOrganizationId(clearanceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Clearance record not found with ID: " + clearanceId));

        if (!clearance.getExit().getId().equals(exitId)) {
            throw new IllegalArgumentException("Clearance record " + clearanceId + " does not belong to exit " + exitId);
        }

        // Enforce capability authorization (department officer or HR/Admin)
        exitAuthService.assertCanActionClearance(currentUser, clearance);

        // Resolve actor
        Employee actor = resolveEmployeeForUser(currentUser, orgId);

        String action = request.getAction() != null ? request.getAction().trim().toUpperCase() : "";
        if (!List.of("CLEAR", "HOLD", "REJECT").contains(action)) {
            throw new IllegalArgumentException("Invalid clearance action: " + request.getAction() + ". Must be CLEAR, HOLD, or REJECT");
        }

        if ("CLEAR".equals(action)) {
            clearance.setStatus("CLEARED");
            clearance.setClearedBy(actor);
            clearance.setClearedAt(LocalDateTime.now());
        } else if ("HOLD".equals(action)) {
            clearance.setStatus("HOLD");
        } else if ("REJECT".equals(action)) {
            clearance.setStatus("REJECTED");
        }

        if (request.getRemarks() != null) {
            clearance.setRemarks(request.getRemarks());
        }

        if (request.getClearanceData() != null) {
            try {
                clearance.setClearanceData(objectMapper.writeValueAsString(request.getClearanceData()));
            } catch (JsonProcessingException e) {
                clearance.setClearanceData(request.getClearanceData().toString());
            }
        }

        clearance.setUpdatedAt(LocalDateTime.now());
        ExitClearance saved = clearanceRepository.save(clearance);

        // Check Clearance Completion Engine
        checkAndAdvanceExitClearanceStatus(clearance.getExit());

        return mapToDto(saved);
    }

    @Transactional
    public void checkAndAdvanceExitClearanceStatus(EmployeeExit exit) {
        List<ExitClearance> clearances = clearanceRepository.findByExitId(exit.getId());

        boolean allCleared = clearances.size() >= 4 && clearances.stream()
                .allMatch(c -> "COMPLETED".equalsIgnoreCase(c.getStatus()) || "CLEARED".equalsIgnoreCase(c.getStatus()));

        if (allCleared) {
            exit.setStatus("FNF_CALCULATION_PENDING");
            exit.setUpdatedAt(LocalDateTime.now());
            exitRepository.save(exit);
        } else {
            // Clearances still in progress - remain in CLEARANCE_PENDING
            exit.setStatus("CLEARANCE_PENDING");
            exit.setUpdatedAt(LocalDateTime.now());
            exitRepository.save(exit);
        }
    }

    private Employee resolveEmployeeForUser(User user, Long orgId) {
        if (user != null && user.getWorkEmail() != null) {
            Optional<Employee> emp = employeeRepository.findByEmail(user.getWorkEmail());
            if (emp.isPresent()) return emp.get();
        }
        return resolveFallbackAssignee(orgId);
    }

    public ExitClearanceDto mapToDto(ExitClearance c) {
        ExitClearanceDto dto = new ExitClearanceDto();
        dto.setClearanceId(c.getId());
        dto.setExitId(c.getExit() != null ? c.getExit().getId() : null);
        dto.setDepartment(c.getDepartment());
        if (c.getAssignedTo() != null) {
            dto.setAssignedToId(c.getAssignedTo().getId());
            dto.setAssignedToName(c.getAssignedTo().getFullName());
            dto.setAssignedToEmail(c.getAssignedTo().getEmail());
        }
        dto.setClearanceReason(c.getClearanceReason());
        dto.setStatus(c.getStatus());
        if (c.getClearedBy() != null) {
            dto.setClearedById(c.getClearedBy().getId());
            dto.setClearedByName(c.getClearedBy().getFullName());
        }
        dto.setClearedAt(c.getClearedAt());
        dto.setRemarks(c.getRemarks());

        if (c.getClearanceData() != null) {
            try {
                dto.setClearanceData(objectMapper.readValue(c.getClearanceData(), Map.class));
            } catch (Exception e) {
                dto.setClearanceData(c.getClearanceData());
            }
        }

        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
