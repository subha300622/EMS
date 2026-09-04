package com.example.ems.organization.event;

public class OrganizationEvents {
    public record OrganizationCreatedEvent(Long organizationId, String code) {}
    public record OrganizationUpdatedEvent(Long organizationId) {}
    public record OrganizationSuspendedEvent(Long organizationId, String reason) {}
    public record OrganizationActivatedEvent(Long organizationId) {}
    public record OrganizationDeletedEvent(Long organizationId, String deletedBy) {}
    public record SubscriptionUpdatedEvent(Long organizationId, String plan, String status) {}
}
