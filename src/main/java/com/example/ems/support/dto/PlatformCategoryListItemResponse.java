package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Platform Support Category List Item")
public class PlatformCategoryListItemResponse {

    @Schema(description = "Category ID", example = "12")
    private Long id;

    @Schema(description = "Category Name", example = "Payroll Support")
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

    public PlatformCategoryListItemResponse() {}

    public PlatformCategoryListItemResponse(Long id, String name, String description,
                                            String status, Integer displayOrder,
                                            Boolean isDefault, Boolean isSystem) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.displayOrder = displayOrder;
        this.isDefault = isDefault;
        this.isSystem = isSystem;
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
}
