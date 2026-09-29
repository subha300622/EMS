package com.example.ems.employee.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.OrganizationAuditLog;
import com.example.ems.organization.repository.OrganizationAuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for recording Employee lifecycle compliance audit entries
 * into the existing {@link OrganizationAuditLog} infrastructure within an independent transaction (REQUIRES_NEW).
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class EmployeeLifecycleAuditService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLifecycleAuditService.class);
    private static final String ENTITY_TYPE = "Employee";

    private final OrganizationAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    public EmployeeLifecycleAuditService(OrganizationAuditLogRepository auditLogRepository,
                                         EmployeeRepository employeeRepository,
                                         ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    public void auditEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeJoinedEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("employeeId", event.employeeId());
        newVals.put("status", "JOINED");

        saveAuditLog(orgId, "EMPLOYEE_JOINED", event.employeeId(), event.occurredAt(), null, toJson(newVals));
    }

    public void auditEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeActivatedEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("status", "ACTIVE");

        saveAuditLog(orgId, "EMPLOYEE_ACTIVATED", event.employeeId(), event.occurredAt(), null, toJson(newVals));
    }

    public void auditEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeTransferredEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> oldVals = new LinkedHashMap<>();
        oldVals.put("departmentId", event.fromDepartmentId());

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("departmentId", event.toDepartmentId());

        saveAuditLog(orgId, "EMPLOYEE_TRANSFERRED", event.employeeId(), event.occurredAt(), toJson(oldVals), toJson(newVals));
    }

    public void auditEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeManagerChangedEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> oldVals = new LinkedHashMap<>();
        oldVals.put("managerId", event.oldManagerId());

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("managerId", event.newManagerId());

        saveAuditLog(orgId, "EMPLOYEE_MANAGER_CHANGED", event.employeeId(), event.occurredAt(), toJson(oldVals), toJson(newVals));
    }

    public void auditEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeSuspendedEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("status", "SUSPENDED");
        if (event.reason() != null) {
            newVals.put("reason", event.reason());
        }

        saveAuditLog(orgId, "EMPLOYEE_SUSPENDED", event.employeeId(), event.occurredAt(), null, toJson(newVals));
    }

    public void auditEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Long orgId = resolveOrganizationId(event.employeeId(), event.organizationId());
        if (orgId == null) {
            log.warn("[LifecycleAudit] Skipping audit for EmployeeTerminatedEvent: organizationId could not be resolved for employee: {}", event.employeeId());
            return;
        }

        Map<String, Object> newVals = new LinkedHashMap<>();
        newVals.put("status", "INACTIVE");
        if (event.reason() != null) {
            newVals.put("reason", event.reason());
        }

        saveAuditLog(orgId, "EMPLOYEE_TERMINATED", event.employeeId(), event.occurredAt(), null, toJson(newVals));
    }

    private void saveAuditLog(Long orgId, String action, Long employeeId, Instant occurredAt, String oldVal, String newVal) {
        OrganizationAuditLog auditLog = new OrganizationAuditLog();
        auditLog.setOrganizationId(orgId);
        auditLog.setAction(action);
        auditLog.setEntity(ENTITY_TYPE);
        auditLog.setEntityId(employeeId);
        auditLog.setPerformedBy(resolveActor());
        auditLog.setPerformedAt(occurredAt != null ? occurredAt : Instant.now());
        auditLog.setOldValues(oldVal);
        auditLog.setNewValues(newVal);

        auditLogRepository.save(auditLog);
        log.info("[LifecycleAudit] Saved audit log for employee: {}, action: {}, orgId: {}", employeeId, action, orgId);
    }

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !auth.getName().isBlank() && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private Long resolveOrganizationId(Long employeeId, Long eventOrgId) {
        if (eventOrgId != null) {
            return eventOrgId;
        }
        if (employeeId == null) {
            return null;
        }
        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isPresent() && empOpt.get().getOrganization() != null) {
            return empOpt.get().getOrganization().getId();
        }
        return null;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("[LifecycleAudit] Failed to serialize audit payload to JSON: {}", obj, e);
            return String.valueOf(obj);
        }
    }
}
