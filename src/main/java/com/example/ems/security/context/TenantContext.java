package com.example.ems.security.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        Long tenantId = currentTenant.get();
        if (tenantId != null) {
            return tenantId;
        }
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest req = attributes.getRequest();
                Object attr = req.getAttribute("organizationId");
                if (attr instanceof Long l) return l;
                if (attr instanceof Number n) return n.longValue();
                if (attr instanceof String s && !s.isBlank()) return Long.parseLong(s);

                String header = req.getHeader("X-Organization-Id");
                if (header != null && !header.isBlank()) {
                    return Long.parseLong(header.trim());
                }
            }
        } catch (Exception ignored) {}
        return null;
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
