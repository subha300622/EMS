package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization summary list item response")
public class OrganizationListItemResponse {
    @Schema(description = "Organization Database ID", example = "1")
    private Long id;

    @Schema(description = "Organization Code", example = "ORG-1001")
    private String organizationCode;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String name;

    @Schema(description = "Primary Contact Email", example = "contact@acme.com")
    private String email;

    @Schema(description = "Primary Contact Phone", example = "+1-555-0199")
    private String phone;

    @Schema(description = "Subscription Plan Name", example = "PREMIUM")
    private String subscriptionPlan;

    @Schema(description = "Active Employee Count", example = "42")
    private long employeeCount;

    @Schema(description = "Account Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created Timestamp", example = "2026-01-01T00:00:00Z")
    private String createdAt;

    public OrganizationListItemResponse() {}

    public OrganizationListItemResponse(Long id, String organizationCode, String name, String email, String phone, String subscriptionPlan, long employeeCount, String status, String createdAt) {
        this.id = id;
        this.organizationCode = organizationCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subscriptionPlan = subscriptionPlan;
        this.employeeCount = employeeCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public long getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(long employeeCount) { this.employeeCount = employeeCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
