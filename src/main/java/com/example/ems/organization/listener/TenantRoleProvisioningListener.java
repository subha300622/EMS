package com.example.ems.organization.listener;

import com.example.ems.organization.event.OrganizationEvents.OrganizationCreatedEvent;
import com.example.ems.organization.service.TenantRoleProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TenantRoleProvisioningListener {

    private static final Logger log = LoggerFactory.getLogger(TenantRoleProvisioningListener.class);

    private final TenantRoleProvisioningService provisioningService;

    public TenantRoleProvisioningListener(TenantRoleProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @EventListener
    public void handleOrganizationCreated(OrganizationCreatedEvent event) {
        log.info("Received OrganizationCreatedEvent for organization ID: {}, code: {}", event.organizationId(), event.code());
        try {
            provisioningService.provisionTenantRoles(event.organizationId());
        } catch (Exception e) {
            log.error("Failed to provision roles for organization ID: " + event.organizationId(), e);
            // In a production application we might publish to DLQ or retry, but logging is correct here
        }
    }
}
