package com.example.ems.asset.dto;

import java.util.List;

public class AssetCategoriesResponse {
    private List<CategoryItem> categories;

    public AssetCategoriesResponse() {}

    public AssetCategoriesResponse(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public List<CategoryItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public static class CategoryItem {
        private Long id;
        private String code;
        private String name;
        private int maximumAllowed;
        private boolean requestEnabled;

        public CategoryItem() {}

        public CategoryItem(Long id, String code, String name, int maximumAllowed, boolean requestEnabled) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.maximumAllowed = maximumAllowed;
            this.requestEnabled = requestEnabled;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getMaximumAllowed() { return maximumAllowed; }
        public void setMaximumAllowed(int maximumAllowed) { this.maximumAllowed = maximumAllowed; }

        public boolean isRequestEnabled() { return requestEnabled; }
        public void setRequestEnabled(boolean requestEnabled) { this.requestEnabled = requestEnabled; }
    }
}
