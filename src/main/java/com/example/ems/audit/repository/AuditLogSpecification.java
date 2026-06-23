package com.example.ems.audit.repository;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuditLogSpecification {

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
                predicates.add(cb.equal(cb.lower(root.get("entityType")), module.trim().toLowerCase()));
            }

            if (action != null && !action.trim().isEmpty() && !"ALL".equalsIgnoreCase(action)) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.trim().toLowerCase()));
            }

            if (user != null && !user.trim().isEmpty() && !"ALL".equalsIgnoreCase(user)) {
                String userLower = "%" + user.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userEmail")), userLower),
                        cb.like(cb.lower(root.get("userName")), userLower)
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
                // To handle case insensitivity or exact matching on list, we do standard IN query.
                // We've seeded using exact case (e.g. "Payroll"), so exact IN is standard.
                predicates.add(root.get("entityType").in(allowedModules));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
