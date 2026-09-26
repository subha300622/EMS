package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Platform Support Category Status Response")
public class PlatformCategoryStatusResponse {

    @Schema(description = "Category ID", example = "12")
    private Long id;

    @Schema(description = "Category Name", example = "Payroll & Salary Support")
    private String name;

    @Schema(description = "Category Status", example = "INACTIVE")
    private String status;

    @Schema(description = "Last update timestamp", example = "2026-09-25T12:15:00+05:30")
    private String updatedAt;

    public PlatformCategoryStatusResponse() {}

    public PlatformCategoryStatusResponse(Long id, String name, String status, String updatedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
