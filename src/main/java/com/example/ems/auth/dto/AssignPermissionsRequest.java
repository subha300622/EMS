package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request body for assigning permissions to a role")
public class AssignPermissionsRequest {

    @Schema(description = "List of permission codes", example = "[\"employee.read\", \"employee.create\"]")
    private List<String> permissions;

    @Schema(description = "List of permission IDs", example = "[1, 2, 3]")
    private List<Long> permissionIds;

    @Schema(description = "List of permission names", example = "[\"employee.read\", \"employee.create\"]")
    private List<String> permissionNames;

    public AssignPermissionsRequest() {}

    public AssignPermissionsRequest(List<String> permissions, List<Long> permissionIds, List<String> permissionNames) {
        this.permissions = permissions;
        this.permissionIds = permissionIds;
        this.permissionNames = permissionNames;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<String> getPermissionNames() {
        return permissionNames;
    }

    public void setPermissionNames(List<String> permissionNames) {
        this.permissionNames = permissionNames;
    }
}
