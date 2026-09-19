package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform Category Dropdown Option")
public class PlatformCategoryOption {
    @Schema(description = "Category ID", example = "1")
    private Long id;
    @Schema(description = "Category Name", example = "Technical Support")
    private String name;
    @Schema(description = "Hex Color", example = "#3B82F6")
    private String color;
    @Schema(description = "Icon identifier", example = "settings")
    private String icon;

    public PlatformCategoryOption() {}

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
