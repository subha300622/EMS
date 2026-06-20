package com.example.ems.finance.dto;

import java.util.List;

public class ReplacementDueAssetsResponse {
    private int count;
    private List<ReplacementDueAssetItem> assets;

    public ReplacementDueAssetsResponse() {}

    public ReplacementDueAssetsResponse(int count, List<ReplacementDueAssetItem> assets) {
        this.count = count;
        this.assets = assets;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ReplacementDueAssetItem> getAssets() {
        return assets;
    }

    public void setAssets(List<ReplacementDueAssetItem> assets) {
        this.assets = assets;
    }
}
