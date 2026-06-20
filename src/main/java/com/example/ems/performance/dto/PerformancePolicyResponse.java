package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class PerformancePolicyResponse {
    @Schema(example = "Project Deliverables")
    private String policyTitle;
    @Schema(example = "string")
    private String lastUpdated;
    private List<PolicySection> sections;
    private List<RatingScale> ratingScales;

    public static class PolicySection {
        @Schema(example = "Project Deliverables")
        private String title;
        @Schema(example = "string")
        private String content;

        public PolicySection(String title, String content) {
            this.title = title;
            this.content = content;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class RatingScale {
        @Schema(example = "1")
        private Integer score;
        @Schema(example = "string")
        private String label;
        @Schema(example = "Detailed description of the item")
        private String description;

        public RatingScale(Integer score, String label, String description) {
            this.score = score;
            this.label = label;
            this.description = description;
        }

        // Getters and Setters
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Getters and Setters
    public String getPolicyTitle() { return policyTitle; }
    public void setPolicyTitle(String policyTitle) { this.policyTitle = policyTitle; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<PolicySection> getSections() { return sections; }
    public void setSections(List<PolicySection> sections) { this.sections = sections; }
    public List<RatingScale> getRatingScales() { return ratingScales; }
    public void setRatingScales(List<RatingScale> ratingScales) { this.ratingScales = ratingScales; }
}
