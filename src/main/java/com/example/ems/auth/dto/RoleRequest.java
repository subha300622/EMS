package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Schema(example = "string")
    private String name;

    @Schema(example = "Detailed description of the item")
    private String description;

    @Schema(example = "[1, 2]")
    private List<Long> permissionGroupIds;

    @Schema(example = "[10, 15]")
    private List<Long> permissionIds;

    @Schema(example = "[\"employee.read\", \"attendance.read\"]")
    private List<String> permissionNames;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Long> getPermissionGroupIds() { return permissionGroupIds; }
    public void setPermissionGroupIds(List<Long> permissionGroupIds) { this.permissionGroupIds = permissionGroupIds; }

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }

    public List<String> getPermissionNames() { return permissionNames; }
    public void setPermissionNames(List<String> permissionNames) { this.permissionNames = permissionNames; }
}
