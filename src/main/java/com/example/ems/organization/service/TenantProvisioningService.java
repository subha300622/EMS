package com.example.ems.organization.service;

import com.example.ems.auth.service.SignupValidationService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Tenant;
import com.example.ems.organization.entity.TenantStatus;
import com.example.ems.organization.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final SignupValidationService validationService;

    public TenantProvisioningService(TenantRepository tenantRepository, SignupValidationService validationService) {
        this.tenantRepository = tenantRepository;
        this.validationService = validationService;
    }

    @Transactional
    public Tenant provisionTenant(Organization organization, String timezone, String currency, String locale) {
        String baseSubdomain = generateBaseSubdomain(organization.getName());
        String subdomain = baseSubdomain;

        int suffix = 2;
        while (validationService.isReservedSubdomain(subdomain) || tenantRepository.existsBySubdomain(subdomain)) {
            subdomain = baseSubdomain + "-" + suffix;
            suffix++;
            if (suffix > 100) {
                throw new IllegalStateException("Unable to generate a unique subdomain for the organization.");
            }
        }

        Tenant tenant = new Tenant();
        tenant.setOrganization(organization);
        tenant.setSubdomain(subdomain);
        tenant.setTimezone(timezone != null ? timezone : "UTC");
        tenant.setCurrency(currency != null ? currency : "USD");
        tenant.setLocale(locale != null ? locale : "en-US");
        tenant.setStatus(TenantStatus.ACTIVE);

        return tenantRepository.save(tenant);
    }

    private String generateBaseSubdomain(String orgName) {
        if (orgName == null || orgName.isBlank()) {
            return "org";
        }
        // Replace non-alphanumeric with hyphens
        String clean = orgName.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-"); // remove duplicate hyphens
        
        // Remove leading/trailing hyphens
        if (clean.startsWith("-")) {
            clean = clean.substring(1);
        }
        if (clean.endsWith("-")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        
        return clean.isBlank() ? "org" : clean;
    }
}
