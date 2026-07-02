package com.example.ems.organization.service;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TenantRoleProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(TenantRoleProvisioningService.class);

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    public TenantRoleProvisioningService(RoleRepository roleRepository,
                                         OrganizationRepository organizationRepository) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public void provisionTenantRoles(Long organizationId) {
        log.info("Starting role provisioning for organization ID: {}", organizationId);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));

        List<Role> platformTemplates = roleRepository.findByIsPlatformTemplateTrue();
        if (platformTemplates.isEmpty()) {
            log.warn("No platform role templates found to provision for organization: {}", organizationId);
            return;
        }

        for (Role template : platformTemplates) {
            // Check if the tenant role already exists to prevent duplicate runs
            if (roleRepository.existsByOrganizationIdAndName(organizationId, template.getName())) {
                log.info("Tenant role '{}' already exists for organization ID: {}, skipping copy.", template.getName(), organizationId);
                continue;
            }

            Role tenantRole = new Role();
            tenantRole.setName(template.getName());
            tenantRole.setDescription(template.getDescription());
            tenantRole.setOrganization(organization);
            tenantRole.setPlatformTemplate(false);
            tenantRole.setSystemRole(template.isSystemRole());
            tenantRole.setVersion(template.getVersion());

            // Duplicate permission links
            Set<Permission> copiedPermissions = new HashSet<>(template.getPermissions());
            tenantRole.setPermissions(copiedPermissions);

            roleRepository.save(tenantRole);
            log.info("Provisioned role '{}' from template for organization ID: {}", tenantRole.getName(), organizationId);
        }

        log.info("Role provisioning completed successfully for organization ID: {}", organizationId);
    }
}
