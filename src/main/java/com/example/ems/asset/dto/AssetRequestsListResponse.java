package com.example.ems.asset.dto;

import java.util.List;

public class AssetRequestsListResponse {
    private List<AssetRequestResponse> requests;

    public AssetRequestsListResponse() {}

    public AssetRequestsListResponse(List<AssetRequestResponse> requests) {
        this.requests = requests;
    }

    public List<AssetRequestResponse> getRequests() {
        return requests;
    }

    public void setRequests(List<AssetRequestResponse> requests) {
        this.requests = requests;
    }
}
