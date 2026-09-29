package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Categories Reorder Request")
public class PlatformCategoryReorderRequest {

    @Schema(description = "List of category display orders")
    @NotNull(message = "Categories list is required")
    @Valid
    private List<CategoryOrderDto> categories;

    public PlatformCategoryReorderRequest() {}

    public List<CategoryOrderDto> getCategories() { return categories; }
    public void setCategories(List<CategoryOrderDto> categories) { this.categories = categories; }

    @Schema(description = "Category order pair")
    public static class CategoryOrderDto {

        @Schema(description = "Category ID", example = "12")
        @NotNull(message = "Category ID is required")
        private Long id;

        @Schema(description = "Display Order index", example = "1")
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
