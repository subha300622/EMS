package com.example.ems.auth.dto;

import java.util.List;

public class AssignPermissionsRequest {

    private List<String> permissions;
    private List<Long> permissionIds;

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
}
