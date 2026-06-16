package com.example.ems.asset.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AssetReturnFormRequest {

    @NotBlank(message = "Return reason is required")
    private String returnReason;

    @NotBlank(message = "Asset condition is required")
    private String assetCondition;

    private List<String> accessoriesReturned;

    private String comments;

    public AssetReturnFormRequest() {}

    public AssetReturnFormRequest(String returnReason, String assetCondition, List<String> accessoriesReturned, String comments) {
        this.returnReason = returnReason;
        this.assetCondition = assetCondition;
        this.accessoriesReturned = accessoriesReturned;
        this.comments = comments;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getAssetCondition() {
        return assetCondition;
    }

    public void setAssetCondition(String assetCondition) {
        this.assetCondition = assetCondition;
    }

    public List<String> getAccessoriesReturned() {
        return accessoriesReturned;
    }

    public void setAccessoriesReturned(List<String> accessoriesReturned) {
        this.accessoriesReturned = accessoriesReturned;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
