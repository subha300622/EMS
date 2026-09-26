package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Platform Category Update Response")
public class PlatformCategoryUpdateResponse {

    @Schema(description = "Category ID", example = "12")
    private Long id;

    @Schema(description = "Category Name", example = "Payroll & Salary Support")
    private String name;

    @Schema(description = "Category Description", example = "Issues related to payroll, salary, payslips and compensation.")
    private String description;

    @Schema(description = "Category Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Display Order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether category is default", example = "false")
    private Boolean isDefault;

    @Schema(description = "Whether category is system protected", example = "false")
    private Boolean isSystem;

    @Schema(description = "Last update timestamp", example = "2026-09-25T12:00:00+05:30")
    private String updatedAt;

    public PlatformCategoryUpdateResponse() {}

    public PlatformCategoryUpdateResponse(Long id, String name, String description, String status,
                                          Integer displayOrder, Boolean isDefault, Boolean isSystem,
                                          String updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.displayOrder = displayOrder;
        this.isDefault = isDefault;
        this.isSystem = isSystem;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
