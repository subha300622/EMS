package com.example.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PermissionGroupDto {

    @JsonProperty("groupId")
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<PermissionResponse> permissions;

    public PermissionGroupDto() {}

    public PermissionGroupDto(Long id, String code, String name, String description, List<PermissionResponse> permissions) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public List<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions;
    }
}
