package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class RemovePermissionResponse {

    @Schema(example = "Software Engineer")
    private Long roleId;
    @Schema(example = "1")
    private Long removedPermissionId;

    public RemovePermissionResponse() {}

    public RemovePermissionResponse(Long roleId, Long removedPermissionId) {
        this.roleId = roleId;
        this.removedPermissionId = removedPermissionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getRemovedPermissionId() {
        return removedPermissionId;
    }

    public void setRemovedPermissionId(Long removedPermissionId) {
        this.removedPermissionId = removedPermissionId;
    }
}
