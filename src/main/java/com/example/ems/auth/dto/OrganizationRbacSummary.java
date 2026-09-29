package com.example.ems.auth.dto;

public class OrganizationRbacSummary {
    private long users;
    private long roles;
    private long customRoles;
    private long permissions;
    private long customizedRoles;
    private int templateVersion;

    public OrganizationRbacSummary() {}

    public OrganizationRbacSummary(long users, long roles, long customRoles, long permissions, long customizedRoles, int templateVersion) {
        this.users = users;
        this.roles = roles;
        this.customRoles = customRoles;
        this.permissions = permissions;
        this.customizedRoles = customizedRoles;
        this.templateVersion = templateVersion;
    }

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getRoles() { return roles; }
    public void setRoles(long roles) { this.roles = roles; }

    public long getCustomRoles() { return customRoles; }
    public void setCustomRoles(long customRoles) { this.customRoles = customRoles; }

    public long getPermissions() { return permissions; }
    public void setPermissions(long permissions) { this.permissions = permissions; }

    public long getCustomizedRoles() { return customizedRoles; }
    public void setCustomizedRoles(long customizedRoles) { this.customizedRoles = customizedRoles; }

    public int getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(int templateVersion) { this.templateVersion = templateVersion; }
}
