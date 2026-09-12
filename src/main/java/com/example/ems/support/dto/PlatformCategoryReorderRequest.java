package com.example.ems.support.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PlatformCategoryReorderRequest {

    @NotNull(message = "Categories list is required")
    @Valid
    private List<CategoryOrderDto> categories;

    public PlatformCategoryReorderRequest() {}

    public List<CategoryOrderDto> getCategories() { return categories; }
    public void setCategories(List<CategoryOrderDto> categories) { this.categories = categories; }

    public static class CategoryOrderDto {
        @NotNull(message = "Category ID is required")
        private Long id;

        @NotNull(message = "Display order is required")
        private Integer displayOrder;

        public CategoryOrderDto() {}

        public CategoryOrderDto(Long id, Integer displayOrder) {
            this.id = id;
            this.displayOrder = displayOrder;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}
