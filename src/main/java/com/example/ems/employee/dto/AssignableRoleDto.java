package com.example.ems.employee.dto;

public class AssignableRoleDto {
    private Long roleId;
    private String roleName;
    private String displayName;

    public AssignableRoleDto() {}
    public AssignableRoleDto(Long roleId, String roleName, String displayName) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.displayName = displayName;
    }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
