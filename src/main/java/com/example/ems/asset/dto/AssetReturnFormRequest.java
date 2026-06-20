package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AssetReturnFormRequest {

    @NotBlank(message = "Return reason is required")
    @Schema(example = "Personal business")
    private String returnReason;

    @NotBlank(message = "Asset condition is required")
    @Schema(example = "string")
    private String assetCondition;

    private List<String> accessoriesReturned;

    @Schema(example = "Excellent progress")
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
