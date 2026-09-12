package com.example.ems.security.tenant;

import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;

@Service
public class TenantSecurityService {

    public Long getRequiredOrganizationId() {
        Long orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new TenantAccessDeniedException("Access Denied: Missing active organization context.");
        }
        return orgId;
    }

    public boolean isTenantOwner(Long targetOrganizationId) {
        Long currentOrgId = TenantContext.getOrganizationId();
        return currentOrgId != null && currentOrgId.equals(targetOrganizationId);
    }

    public void validateTenantAccess(Long targetOrganizationId, boolean isPlatformAdmin) {
        if (isPlatformAdmin) {
            return; // Platform Admin bypass
        }
        Long currentOrgId = getRequiredOrganizationId();
        if (!currentOrgId.equals(targetOrganizationId)) {
            throw new TenantAccessDeniedException("Cross-tenant access forbidden: Target organization does not match active tenant.");
        }
    }
}
