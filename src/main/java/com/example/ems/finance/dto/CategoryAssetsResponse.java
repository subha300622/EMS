package com.example.ems.finance.dto;

import java.util.List;

public class CategoryAssetsResponse {
    private Long categoryId;
    private String categoryName;
    private List<CategoryAssetItem> assets;

    public CategoryAssetsResponse() {}

    public CategoryAssetsResponse(Long categoryId, String categoryName, List<CategoryAssetItem> assets) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.assets = assets;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<CategoryAssetItem> getAssets() {
        return assets;
    }

    public void setAssets(List<CategoryAssetItem> assets) {
        this.assets = assets;
    }
}
