package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class PermissionResponse {

    @Schema(example = "1")
    private Long permissionId;
    @Schema(example = "string")
    private String name;
    @Schema(example = "Detailed description of the item")
    private String description;

    public PermissionResponse() {}

    public PermissionResponse(Long permissionId, String name, String description) {
        this.permissionId = permissionId;
        this.name = name;
        this.description = description;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
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
}
