package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Category Status Update Request")
public class CategoryStatusUpdateRequest {

    @Schema(description = "Category status", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    private String status;

    public CategoryStatusUpdateRequest() {}

    public CategoryStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
