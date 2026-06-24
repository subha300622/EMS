package com.example.ems.performance.manager.dto;

import java.util.List;

public class SaveManagerRatingRequest {
    private List<CompetencyRatingInput> competencyRatings;
    private String managerComment;
    private String recommendation;

    public List<CompetencyRatingInput> getCompetencyRatings() { return competencyRatings; }
    public void setCompetencyRatings(List<CompetencyRatingInput> competencyRatings) { this.competencyRatings = competencyRatings; }

    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public static class CompetencyRatingInput {
        private String competency;
        private Integer score;
        private String comment;

        public String getCompetency() { return competency; }
        public void setCompetency(String competency) { this.competency = competency; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
