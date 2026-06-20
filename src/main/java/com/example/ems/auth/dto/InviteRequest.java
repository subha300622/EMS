package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;




import jakarta.validation.constraints.NotNull;

public class InviteRequest {
    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotNull(message = "Role ID is required")
    @Schema(example = "Software Engineer")
    private Long roleId;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
