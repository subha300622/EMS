package com.example.ems.auth.dto;

public class PlatformDashboardResponse {
    private long organizations;
    private long newOrganizationsThisMonth;
    private long users;
    private long activeUsersToday;
    private long newUsersThisWeek;
    private long roles;
    private long customRoles;
    private long permissions;
    private long activeOrganizations;

    public PlatformDashboardResponse() {}

    public PlatformDashboardResponse(long organizations, long newOrganizationsThisMonth, long users,
                                     long activeUsersToday, long newUsersThisWeek, long roles,
                                     long customRoles, long permissions, long activeOrganizations) {
        this.organizations = organizations;
        this.newOrganizationsThisMonth = newOrganizationsThisMonth;
        this.users = users;
        this.activeUsersToday = activeUsersToday;
        this.newUsersThisWeek = newUsersThisWeek;
        this.roles = roles;
        this.customRoles = customRoles;
        this.permissions = permissions;
        this.activeOrganizations = activeOrganizations;
    }

    public long getOrganizations() { return organizations; }
    public void setOrganizations(long organizations) { this.organizations = organizations; }

    public long getNewOrganizationsThisMonth() { return newOrganizationsThisMonth; }
    public void setNewOrganizationsThisMonth(long newOrganizationsThisMonth) { this.newOrganizationsThisMonth = newOrganizationsThisMonth; }

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getActiveUsersToday() { return activeUsersToday; }
    public void setActiveUsersToday(long activeUsersToday) { this.activeUsersToday = activeUsersToday; }

    public long getNewUsersThisWeek() { return newUsersThisWeek; }
    public void setNewUsersThisWeek(long newUsersThisWeek) { this.newUsersThisWeek = newUsersThisWeek; }

    public long getRoles() { return roles; }
    public void setRoles(long roles) { this.roles = roles; }

    public long getCustomRoles() { return customRoles; }
    public void setCustomRoles(long customRoles) { this.customRoles = customRoles; }

    public long getPermissions() { return permissions; }
    public void setPermissions(long permissions) { this.permissions = permissions; }

    public long getActiveOrganizations() { return activeOrganizations; }
    public void setActiveOrganizations(long activeOrganizations) { this.activeOrganizations = activeOrganizations; }
}
