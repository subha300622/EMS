package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Category Create / Update Request")
public class PlatformCategoryRequest {

    @Schema(description = "Category name", example = "Payroll Support", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is required")
    private String name;

    @Schema(description = "Category description", example = "Issues related to payroll, salary, payslips and compensation.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

   

    @Schema(hidden = true)
    private String status; // ACTIVE, INACTIVE

    public PlatformCategoryRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
