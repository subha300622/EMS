package com.example.ems.employee.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignBulkRolesRequest {
    @NotEmpty(message = "Role IDs list cannot be empty")
    private List<Long> roleIds;

    public AssignBulkRolesRequest() {}
    public AssignBulkRolesRequest(List<Long> roleIds) { this.roleIds = roleIds; }

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
