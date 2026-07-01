package com.example.ems.organization.dto;

public class OrganizationListItemResponse {
    private Long id;
    private String organizationCode;
    private String name;
    private String email;
    private String phone;
    private String subscriptionPlan;
    private long employeeCount;
    private String status;
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
