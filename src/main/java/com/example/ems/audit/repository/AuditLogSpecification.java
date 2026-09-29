package com.example.ems.audit.repository;

import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuditLogSpecification {

    /**
     * Legacy filter method preserved for existing callers and backward compatibility.
     */
    public static Specification<AuditLog> filter(
            String search,
            String module,
            String action,
            String user,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Severity severity,
            Boolean flagged,
            Collection<String> allowedModules
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("userEmail")), searchLower),
                        cb.like(cb.lower(root.get("userName")), searchLower),
                        cb.like(cb.lower(root.get("action")), searchLower),
                        cb.like(cb.lower(root.get("details")), searchLower),
                        cb.like(cb.lower(root.get("ipAddress")), searchLower)
                );
                predicates.add(searchPredicate);
            }

            if (module != null && !module.trim().isEmpty() && !"ALL".equalsIgnoreCase(module)) {
                String mod = module.trim().toLowerCase();
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("module")), mod),
                        cb.equal(cb.lower(root.get("entityType")), mod)
                ));
            }

            if (action != null && !action.trim().isEmpty() && !"ALL".equalsIgnoreCase(action)) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.trim().toLowerCase()));
            }

            if (user != null && !user.trim().isEmpty() && !"ALL".equalsIgnoreCase(user)) {
                String userLower = "%" + user.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userEmail")), userLower),
                        cb.like(cb.lower(root.get("userName")), userLower),
                        cb.like(cb.lower(root.get("userId")), userLower)
                ));
            }

            if (startDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            if (endDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            if (flagged != null) {
                predicates.add(cb.equal(root.get("flagged"), flagged));
            }

            if (allowedModules != null && !allowedModules.isEmpty()) {
                predicates.add(cb.or(
                        root.get("module").in(allowedModules),
                        root.get("entityType").in(allowedModules)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Enterprise filter specification with multi-tenant company isolation, department scoping,
     * and structured filter criteria.
     */
    public static Specification<AuditLog> filter(
            AuditLogFilterRequest request,
            Long enforcedCompanyId,
            Long departmentScope,
            Collection<String> allowedModules
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tenant isolation
            if (enforcedCompanyId != null) {
                predicates.add(cb.equal(root.get("companyId"), enforcedCompanyId));
            } else if (request != null && request.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), request.getCompanyId()));
            }

            // Department scoping (for department managers)
            if (departmentScope != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentScope));
            } else if (request != null && request.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), request.getDepartmentId()));
            }

            // Allowed modules scoping
            if (allowedModules != null && !allowedModules.isEmpty()) {
                predicates.add(cb.or(
                        root.get("module").in(allowedModules),
                        root.get("entityType").in(allowedModules)
                ));
            }

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (request.getModule() != null && !request.getModule().trim().isEmpty() && !"ALL".equalsIgnoreCase(request.getModule())) {
                String mod = request.getModule().trim().toLowerCase();
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("module")), mod),
                        cb.equal(cb.lower(root.get("entityType")), mod)
                ));
            }

            if (request.getAction() != null && !request.getAction().trim().isEmpty() && !"ALL".equalsIgnoreCase(request.getAction())) {
                predicates.add(cb.equal(cb.lower(root.get("action")), request.getAction().trim().toLowerCase()));
            }

            if (request.getStatus() != null && !request.getStatus().trim().isEmpty() && !"ALL".equalsIgnoreCase(request.getStatus())) {
                predicates.add(cb.equal(cb.lower(root.get("status")), request.getStatus().trim().toLowerCase()));
            }

            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("userId"), request.getUserId().trim()));
            }

            if (request.getEntityType() != null && !request.getEntityType().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("entityType")), request.getEntityType().trim().toLowerCase()));
            }

            if (request.getRecordId() != null && !request.getRecordId().trim().isEmpty()) {
                String recId = request.getRecordId().trim();
                predicates.add(cb.or(
                        cb.equal(root.get("recordId"), recId),
                        cb.equal(root.get("entityId"), recId)
                ));
            }

            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }

            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }

            if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
                String searchLower = "%" + request.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userEmail")), searchLower),
                        cb.like(cb.lower(root.get("userName")), searchLower),
                        cb.like(cb.lower(root.get("userId")), searchLower),
                        cb.like(cb.lower(root.get("action")), searchLower),
                        cb.like(cb.lower(root.get("details")), searchLower),
                        cb.like(cb.lower(root.get("ipAddress")), searchLower),
                        cb.like(cb.lower(root.get("recordId")), searchLower),
                        cb.like(cb.lower(root.get("entityId")), searchLower),
                        cb.like(cb.lower(root.get("module")), searchLower),
                        cb.like(cb.lower(root.get("entityType")), searchLower)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
