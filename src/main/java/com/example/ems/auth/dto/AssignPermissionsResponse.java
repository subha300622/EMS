package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AssignPermissionsResponse {

    @Schema(example = "Software Engineer")
    private Long roleId;
    private List<String> assignedPermissions;

    public AssignPermissionsResponse() {}

    public AssignPermissionsResponse(Long roleId, List<String> assignedPermissions) {
        this.roleId = roleId;
        this.assignedPermissions = assignedPermissions;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<String> getAssignedPermissions() {
        return assignedPermissions;
    }

    public void setAssignedPermissions(List<String> assignedPermissions) {
        this.assignedPermissions = assignedPermissions;
    }
}
