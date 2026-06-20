package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {

    @Schema(example = "Software Engineer")
    private Long roleId;
    @Schema(example = "string")
    private String name;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "1")
    private Integer permissionsCount;
    @Schema(example = "string")
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
