package com.example.ems.asset.dto;

import java.util.List;

public class AssetIssuesListResponse {
    private List<ReportIssueResponse> issues;

    public AssetIssuesListResponse() {}

    public AssetIssuesListResponse(List<ReportIssueResponse> issues) {
        this.issues = issues;
    }

    public List<ReportIssueResponse> getIssues() {
        return issues;
    }

    public void setIssues(List<ReportIssueResponse> issues) {
        this.issues = issues;
    }
}
