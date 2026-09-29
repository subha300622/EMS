package com.example.ems.auth.dto;

import java.util.List;

public class PermissionCatalogResponseDto {

    private List<PermissionGroupDto> groups;
    private List<PermissionResponse> standalonePermissions;

    public PermissionCatalogResponseDto() {}

    public PermissionCatalogResponseDto(List<PermissionGroupDto> groups, List<PermissionResponse> standalonePermissions) {
        this.groups = groups;
        this.standalonePermissions = standalonePermissions;
    }

    public List<PermissionGroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<PermissionGroupDto> groups) {
        this.groups = groups;
    }

    public List<PermissionResponse> getStandalonePermissions() {
        return standalonePermissions;
    }

    public void setStandalonePermissions(List<PermissionResponse> standalonePermissions) {
        this.standalonePermissions = standalonePermissions;
    }
}
