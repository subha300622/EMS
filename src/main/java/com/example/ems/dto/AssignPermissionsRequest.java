package com.example.ems.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignPermissionsRequest {

    @NotEmpty(message = "Permissions list cannot be empty")
    private List<String> permissions;

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
