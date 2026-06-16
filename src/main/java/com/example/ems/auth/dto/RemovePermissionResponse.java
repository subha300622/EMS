package com.example.ems.auth.dto;

public class RemovePermissionResponse {

    private Long roleId;
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
