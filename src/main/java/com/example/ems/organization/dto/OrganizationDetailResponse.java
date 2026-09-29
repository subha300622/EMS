package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed organization information response")
public class OrganizationDetailResponse {
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

    @Schema(description = "Website URL", example = "https://acme.com")
    private String website;

    @Schema(description = "Physical address")
    private OrganizationAddressDto address;

    @Schema(description = "Subscription details")
    private OrganizationSubscriptionDto subscription;

    @Schema(description = "Total Employee Count", example = "150")
    private long employeeCount;

    @Schema(description = "Total Admin Count", example = "3")
    private long adminCount;

    @Schema(description = "Registration Date/Time", example = "2026-01-01T00:00:00Z")
    private String createdAt;

    public OrganizationDetailResponse() {}

    public OrganizationDetailResponse(Long id, String organizationCode, String name, String email, String phone, String website, OrganizationAddressDto address, OrganizationSubscriptionDto subscription, long employeeCount, long adminCount, String createdAt) {
        this.id = id;
        this.organizationCode = organizationCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.website = website;
        this.address = address;
        this.subscription = subscription;
        this.employeeCount = employeeCount;
        this.adminCount = adminCount;
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

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public OrganizationAddressDto getAddress() { return address; }
    public void setAddress(OrganizationAddressDto address) { this.address = address; }

    public OrganizationSubscriptionDto getSubscription() { return subscription; }
    public void setSubscription(OrganizationSubscriptionDto subscription) { this.subscription = subscription; }

    public long getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(long employeeCount) { this.employeeCount = employeeCount; }

    public long getAdminCount() { return adminCount; }
    public void setAdminCount(long adminCount) { this.adminCount = adminCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
