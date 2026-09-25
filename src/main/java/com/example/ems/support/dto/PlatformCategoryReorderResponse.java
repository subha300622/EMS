package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform Category Reorder Response")
public class PlatformCategoryReorderResponse {

    @Schema(description = "Status message", example = "Categories reordered successfully")
    private String message;

    public PlatformCategoryReorderResponse() {}

    public PlatformCategoryReorderResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
