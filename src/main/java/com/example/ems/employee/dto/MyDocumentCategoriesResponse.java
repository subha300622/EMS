package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MyDocumentCategoriesResponse {

    private List<CategoryItem> categories;

    public MyDocumentCategoriesResponse() {}

    public MyDocumentCategoriesResponse(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public List<CategoryItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public static class CategoryItem {
        @Schema(example = "1")
        private Long categoryId;
        @Schema(example = "string")
        private String name;
        @Schema(example = "string")
        private String icon;
        @Schema(example = "1")
        private int uploaded;
        @Schema(example = "1")
        private int total;
        @Schema(example = "1")
        private int completionPercentage;

        public CategoryItem() {}

        public CategoryItem(Long categoryId, String name, String icon, int uploaded, int total, int completionPercentage) {
            this.categoryId = categoryId;
            this.name = name;
            this.icon = icon;
            this.uploaded = uploaded;
            this.total = total;
            this.completionPercentage = completionPercentage;
        }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public int getUploaded() { return uploaded; }
        public void setUploaded(int uploaded) { this.uploaded = uploaded; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    }
}
