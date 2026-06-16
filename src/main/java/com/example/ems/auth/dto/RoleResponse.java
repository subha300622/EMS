package com.example.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {

    private Long roleId;
    private String name;
    private String description;
    private Integer permissionsCount;
    private String createdAt;
    private List<PermissionResponse> permissions;

    public RoleResponse() {}

    public RoleResponse(Long roleId, String name, String description, Integer permissionsCount, String createdAt, List<PermissionResponse> permissions) {
        this.roleId = roleId;
        this.name = name;
        this.description = description;
        this.permissionsCount = permissionsCount;
        this.createdAt = createdAt;
        this.permissions = permissions;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPermissionsCount() {
        return permissionsCount;
    }

    public void setPermissionsCount(Integer permissionsCount) {
        this.permissionsCount = permissionsCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions;
    }
}
