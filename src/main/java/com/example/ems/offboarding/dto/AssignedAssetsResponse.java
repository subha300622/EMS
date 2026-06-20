package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AssignedAssetsResponse {

    private List<AssetItem> assets;

    public AssignedAssetsResponse() {}

    public AssignedAssetsResponse(List<AssetItem> assets) {
        this.assets = assets;
    }

    public List<AssetItem> getAssets() { return assets; }
    public void setAssets(List<AssetItem> assets) { this.assets = assets; }

    public static class AssetItem {
        @Schema(example = "1")
        private Long assetId;
        @Schema(example = "string")
        private String assetName;
        @Schema(example = "string")
        private String assetCategory;
        @Schema(example = "string")
        private String serialNumber;
        @Schema(example = "ACTIVE")
        private String returnStatus;

        public AssetItem() {}

        public AssetItem(Long assetId, String assetName, String assetCategory, String serialNumber, String returnStatus) {
            this.assetId = assetId;
            this.assetName = assetName;
            this.assetCategory = assetCategory;
            this.serialNumber = serialNumber;
            this.returnStatus = returnStatus;
        }

        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }

        public String getAssetCategory() { return assetCategory; }
        public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }

        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

        public String getReturnStatus() { return returnStatus; }
        public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
    }
}
