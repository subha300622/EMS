package com.example.ems.auth.dto;

public class PlatformOrganizationSummaryResponse {
    private Long organizationId;
    private String name;
    private long userCount;
    private long roleCount;
    private long customRoleCount;
    private long permissionCount;
    private String subscription;
    private String status;

    public PlatformOrganizationSummaryResponse() {}

    public PlatformOrganizationSummaryResponse(Long organizationId, String name, long userCount, long roleCount,
                                               long customRoleCount, long permissionCount, String subscription, String status) {
        this.organizationId = organizationId;
        this.name = name;
        this.userCount = userCount;
        this.roleCount = roleCount;
        this.customRoleCount = customRoleCount;
        this.permissionCount = permissionCount;
        this.subscription = subscription;
        this.status = status;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getUserCount() { return userCount; }
    public void setUserCount(long userCount) { this.userCount = userCount; }

    public long getRoleCount() { return roleCount; }
    public void setRoleCount(long roleCount) { this.roleCount = roleCount; }

    public long getCustomRoleCount() { return customRoleCount; }
    public void setCustomRoleCount(long customRoleCount) { this.customRoleCount = customRoleCount; }

    public long getPermissionCount() { return permissionCount; }
    public void setPermissionCount(long permissionCount) { this.permissionCount = permissionCount; }

    public String getSubscription() { return subscription; }
    public void setSubscription(String subscription) { this.subscription = subscription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
