package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AssignRoleRequest {

    @Schema(example = "Software Engineer")
    private String role;
    @Schema(example = "Software Engineer")
    private String roleName;
    @Schema(example = "Software Engineer")
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
