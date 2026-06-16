package com.example.ems.expense.dto;

import java.time.LocalDate;
import java.util.List;

public class ExpensePoliciesResponse {
    private List<PolicyItem> policies;

    public ExpensePoliciesResponse() {}

    public ExpensePoliciesResponse(List<PolicyItem> policies) {
        this.policies = policies;
    }

    public List<PolicyItem> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PolicyItem> policies) {
        this.policies = policies;
    }

    public static class PolicyItem {
        private Long policyId;
        private String title;
        private String description;
        private LocalDate effectiveFrom;
        private String version;

        public PolicyItem() {}

        public PolicyItem(Long policyId, String title, String description, LocalDate effectiveFrom, String version) {
            this.policyId = policyId;
            this.title = title;
            this.description = description;
            this.effectiveFrom = effectiveFrom;
            this.version = version;
        }

        public Long getPolicyId() { return policyId; }
        public void setPolicyId(Long policyId) { this.policyId = policyId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDate getEffectiveFrom() { return effectiveFrom; }
        public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}
