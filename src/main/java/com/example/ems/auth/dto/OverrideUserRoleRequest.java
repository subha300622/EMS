package com.example.ems.auth.dto;

public class OverrideUserRoleRequest {
    private String roleName;
    private Long roleId;
    private String reason;

    public OverrideUserRoleRequest() {}

    public OverrideUserRoleRequest(String roleName, Long roleId, String reason) {
        this.roleName = roleName;
        this.roleId = roleId;
        this.reason = reason;
    }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
