package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AssetPoliciesResponse {
    private List<PolicyItem> policies;

    public AssetPoliciesResponse() {}

    public AssetPoliciesResponse(List<PolicyItem> policies) {
        this.policies = policies;
    }

    public List<PolicyItem> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PolicyItem> policies) {
        this.policies = policies;
    }

    public static class PolicyItem {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "Project Deliverables")
        private String title;
        @Schema(example = "Detailed description of the item")
        private String description;
        @Schema(example = "true")
        private boolean acknowledged;

        public PolicyItem() {}

        public PolicyItem(Long id, String title, String description, boolean acknowledged) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.acknowledged = acknowledged;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isAcknowledged() { return acknowledged; }
        public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    }
}
