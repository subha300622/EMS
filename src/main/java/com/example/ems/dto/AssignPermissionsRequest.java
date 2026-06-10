package com.example.ems.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {

    @NotEmpty(message = "Permissions list cannot be empty")
    private List<String> permissions;
}
