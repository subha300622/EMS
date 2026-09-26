package com.example.ems.audit.repository;

import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuditLogSpecificationTest {

    private Root<AuditLog> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;
    private Path<Object> path;
    private Expression<String> stringExpr;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
        path = mock(Path.class);
        stringExpr = mock(Expression.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.lower(any())).thenReturn(stringExpr);
        when(cb.or(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(), any(LocalDateTime.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(), any(LocalDateTime.class))).thenReturn(mock(Predicate.class));
    }

    @Test
    public void testLegacyFilterSpecification() {
        Specification<AuditLog> spec = AuditLogSpecification.filter(
                "john", "EMPLOYEE", "CREATE", "john@example.com",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                Severity.INFO, false, List.of("EMPLOYEE", "LEAVE")
        );

        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }

    @Test
    public void testEnterpriseFilterWithTenantAndDepartmentScoping() {
        AuditLogFilterRequest request = new AuditLogFilterRequest();
        request.setModule("EMPLOYEE");
        request.setAction("UPDATE");
        request.setStatus("SUCCESS");
        request.setRecordId("EMP001");
        request.setSearch("test query");
        request.setStartDate(LocalDateTime.now().minusDays(2));
        request.setEndDate(LocalDateTime.now());

        Specification<AuditLog> spec = AuditLogSpecification.filter(
                request,
                10L, // Enforced company ID
                5L,  // Enforced department scope
                List.of("EMPLOYEE")
        );

        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }
}
