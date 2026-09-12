package com.example.ems.auth.dto;

public class OrgDashboardResponse {
    private long users;
    private long roles;
    private long customRoles;
    private long permissions;

    public OrgDashboardResponse() {}

    public OrgDashboardResponse(long users, long roles, long customRoles, long permissions) {
        this.users = users;
        this.roles = roles;
        this.customRoles = customRoles;
        this.permissions = permissions;
    }

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getRoles() { return roles; }
    public void setRoles(long roles) { this.roles = roles; }

    public long getCustomRoles() { return customRoles; }
    public void setCustomRoles(long customRoles) { this.customRoles = customRoles; }

    public long getPermissions() { return permissions; }
    public void setPermissions(long permissions) { this.permissions = permissions; }
}
