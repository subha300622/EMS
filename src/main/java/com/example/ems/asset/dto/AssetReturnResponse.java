package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class AssetReturnResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String returnReference;
    @Schema(example = "1")
    private Long assetId;
    @Schema(example = "string")
    private String assetName;
    @Schema(example = "Personal business")
    private String returnReason;
    @Schema(example = "string")
    private String assetCondition;
    private List<String> accessoriesReturned;
    @Schema(example = "Excellent progress")
    private String comments;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime requestedAt;

    public AssetReturnResponse() {}

    public AssetReturnResponse(Long id, String returnReference, Long assetId, String assetName, String returnReason, String assetCondition, List<String> accessoriesReturned, String comments, String status, LocalDateTime requestedAt) {
        this.id = id;
        this.returnReference = returnReference;
        this.assetId = assetId;
        this.assetName = assetName;
        this.returnReason = returnReason;
        this.assetCondition = assetCondition;
        this.accessoriesReturned = accessoriesReturned;
        this.comments = comments;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReturnReference() { return returnReference; }
    public void setReturnReference(String returnReference) { this.returnReference = returnReference; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public String getAssetCondition() { return assetCondition; }
    public void setAssetCondition(String assetCondition) { this.assetCondition = assetCondition; }

    public List<String> getAccessoriesReturned() { return accessoriesReturned; }
    public void setAccessoriesReturned(List<String> accessoriesReturned) { this.accessoriesReturned = accessoriesReturned; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
