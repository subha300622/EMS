package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class SupportCategoryResponse {
    @Schema(example = "1")
    private Long categoryId;
    @Schema(example = "string")
    private String name;
    @Schema(example = "string")
    private String icon;
    private List<SubCategoryDto> subCategories;

    public SupportCategoryResponse() {}

    public SupportCategoryResponse(Long categoryId, String name, String icon, List<SubCategoryDto> subCategories) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.subCategories = subCategories;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<SubCategoryDto> getSubCategories() { return subCategories; }
    public void setSubCategories(List<SubCategoryDto> subCategories) { this.subCategories = subCategories; }

    public static class SubCategoryDto {
        @Schema(example = "1")
        private Long subCategoryId;
        @Schema(example = "string")
        private String name;

        public SubCategoryDto() {}

        public SubCategoryDto(Long subCategoryId, String name) {
            this.subCategoryId = subCategoryId;
            this.name = name;
        }

        public Long getSubCategoryId() { return subCategoryId; }
        public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
