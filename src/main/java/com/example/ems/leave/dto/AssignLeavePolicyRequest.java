package com.example.ems.leave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request body for assigning leave policy to employees")
public record AssignLeavePolicyRequest(
    @Schema(description = "List of Employee IDs to assign policy to", example = "[1, 2, 3]")
    List<Long> employeeIds,

    @Schema(description = "Department ID if assigning by department", example = "5")
    Long departmentId
) {}
