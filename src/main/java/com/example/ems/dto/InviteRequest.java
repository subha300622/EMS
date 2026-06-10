package com.example.ems.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
