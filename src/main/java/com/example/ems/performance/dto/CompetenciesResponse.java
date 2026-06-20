package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class CompetenciesResponse {
    private List<CompetencyItem> competencies;

    public static class CompetencyItem {
        @Schema(example = "string")
        private String name;
        @Schema(example = "string")
        private String type;
        @Schema(example = "1")
        private Integer expectedLevel;
        @Schema(example = "1")
        private Integer currentLevel;
        @Schema(example = "1")
        private Integer gap;
        @Schema(example = "string")
        private String improvementPlan;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getExpectedLevel() { return expectedLevel; }
        public void setExpectedLevel(Integer expectedLevel) { this.expectedLevel = expectedLevel; }
        public Integer getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }
        public Integer getGap() { return gap; }
        public void setGap(Integer gap) { this.gap = gap; }
        public String getImprovementPlan() { return improvementPlan; }
        public void setImprovementPlan(String improvementPlan) { this.improvementPlan = improvementPlan; }
    }

    public List<CompetencyItem> getCompetencies() { return competencies; }
    public void setCompetencies(List<CompetencyItem> competencies) { this.competencies = competencies; }
}
