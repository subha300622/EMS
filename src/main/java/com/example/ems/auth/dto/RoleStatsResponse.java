package com.example.ems.auth.dto;

public class RoleStatsResponse {
    private String role;
    private long users;
    private long permissions;
    private boolean customized;

    public RoleStatsResponse() {}

    public RoleStatsResponse(String role, long users, long permissions, boolean customized) {
        this.role = role;
        this.users = users;
        this.permissions = permissions;
        this.customized = customized;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getPermissions() { return permissions; }
    public void setPermissions(long permissions) { this.permissions = permissions; }

    public boolean isCustomized() { return customized; }
    public void setCustomized(boolean customized) { this.customized = customized; }
}
