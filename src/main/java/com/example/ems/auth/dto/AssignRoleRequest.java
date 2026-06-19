package com.example.ems.auth.dto;

public class AssignRoleRequest {

    private String role;
    private String roleName;
    private Long roleId;

    public String getRole() {
        return roleName != null ? roleName : role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
