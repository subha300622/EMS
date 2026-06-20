package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public class ExpenseCategoriesResponse {
    private List<CategoryItem> categories;

    public ExpenseCategoriesResponse() {}

    public ExpenseCategoriesResponse(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public List<CategoryItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public static class CategoryItem {
        @Schema(example = "EMP101")
        private String code;
        @Schema(example = "string")
        private String name;
        @Schema(example = "100.00")
        private BigDecimal maxLimit;
        @Schema(example = "true")
        private boolean requiresReceipt;

        public CategoryItem() {}

        public CategoryItem(String code, String name, BigDecimal maxLimit, boolean requiresReceipt) {
            this.code = code;
            this.name = name;
            this.maxLimit = maxLimit;
            this.requiresReceipt = requiresReceipt;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getMaxLimit() { return maxLimit; }
        public void setMaxLimit(BigDecimal maxLimit) { this.maxLimit = maxLimit; }

        public boolean isRequiresReceipt() { return requiresReceipt; }
        public void setRequiresReceipt(boolean requiresReceipt) { this.requiresReceipt = requiresReceipt; }
    }
}
