package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Support Category Dropdown Option")
public class PlatformCategoryOption {

    @Schema(description = "Category ID", example = "12")
    private Long id;

    @Schema(description = "Category Name", example = "Payroll Support")
    private String name;

    @Schema(hidden = true)
    private String color;

    @Schema(hidden = true)
    private String icon;

    public PlatformCategoryOption() {}

    public PlatformCategoryOption(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public PlatformCategoryOption(Long id, String name, String color, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
