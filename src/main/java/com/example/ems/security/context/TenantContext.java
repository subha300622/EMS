package com.example.ems.security.context;

/**
 * Thread-local context to store the current tenant (organization) ID.
 */
public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return currentTenant.get();
    }

    public static Long getOrganizationId() {
        return currentTenant.get();
    }

    public static Long requireOrganizationId() {
        Long tenantId = currentTenant.get();
        if (tenantId == null) {
            throw new IllegalStateException("Access Denied: No active organization context found.");
        }
        return tenantId;
    }

    public static void clear() {
        currentTenant.remove();
    }
}
