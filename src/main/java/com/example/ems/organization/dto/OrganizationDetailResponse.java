package com.example.ems.organization.dto;

public class OrganizationDetailResponse {
    private Long id;
    private String organizationCode;
    private String name;
    private String email;
    private String phone;
    private String website;
    private OrganizationAddressDto address;
    private OrganizationSubscriptionDto subscription;
    private long employeeCount;
    private long adminCount;
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
