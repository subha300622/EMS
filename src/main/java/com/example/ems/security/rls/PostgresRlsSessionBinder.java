package com.example.ems.security.rls;

import com.example.ems.security.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages PostgreSQL Row-Level Security (RLS) session variable binding.
 * Uses transaction-scoped 'SET LOCAL' to ensure configuration automatically
 * expires upon commit or rollback, preventing tenant leakage across HikariCP pooled connections.
 */
@Component
public class PostgresRlsSessionBinder {

    private static final Logger log = LoggerFactory.getLogger(PostgresRlsSessionBinder.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Binds tenant context to the current transaction.
     * Fails closed if tenantId is null and caller is not a platform admin.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void bindTenantToCurrentTransaction(Long tenantId, boolean isPlatformAdmin) {
        if (isPlatformAdmin) {
            log.debug("PostgresRlsSessionBinder: Binding platform admin bypass to transaction.");
            entityManager.createNativeQuery("SET LOCAL app.is_platform_admin = 'true'").executeUpdate();
            return;
        }

        if (tenantId == null) {
            log.error("PostgresRlsSessionBinder: Attempted to bind RLS with null tenant ID - failing closed.");
            throw new IllegalStateException("Cannot bind RLS context: Missing required tenant ID.");
        }

        log.debug("PostgresRlsSessionBinder: Binding tenant {} to transaction via SET LOCAL.", tenantId);
        entityManager.createNativeQuery("SET LOCAL app.current_tenant_id = '" + tenantId + "'")
                .executeUpdate();
    }

    /**
     * Binds the current TenantContext's organizationId to the transaction.
     * Fails closed if TenantContext has no active organization ID.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void bindCurrentTenant() {
        Long orgId = TenantContext.requireOrganizationId();
        bindTenantToCurrentTransaction(orgId, false);
    }
}
