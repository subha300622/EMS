package com.example.ems.employee.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRoleRequest {
    @NotNull(message = "Role ID is required")
    private Long roleId;

    public AssignRoleRequest() {}
    public AssignRoleRequest(Long roleId) { this.roleId = roleId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
