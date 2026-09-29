package com.example.ems.performance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Employee Self-Review Submission Payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseSelfReviewRequest {

    @Schema(description = "Employee self-evaluation rating (1.00 - 5.00)", example = "4.20")
    private BigDecimal selfScore;

    @Schema(description = "Self-evaluation feedback and summary of achievements", example = "Exceeded delivery goals for Q3 projects and mentored new joiners.")
    private String selfFeedback;

    @Schema(description = "General comments on self review", example = "Employee self review completed")
    private String comments;

    @Schema(description = "Summary of achievements", example = "Delivered assigned projects successfully")
    private String achievements;

    @Schema(description = "Areas for future development", example = "Improve leadership and mentoring")
    private String developmentAreas;

    @Schema(description = "Idempotency key to ensure safe submission retry", example = "7c9a12b3-4567")
    private String idempotencyKey;

    public EnterpriseSelfReviewRequest() {}

    public EnterpriseSelfReviewRequest(BigDecimal selfScore, String selfFeedback) {
        this.selfScore = selfScore;
        this.selfFeedback = selfFeedback;
    }

    public BigDecimal getSelfScore() {
        if (selfScore == null) {
            return BigDecimal.valueOf(4.00);
        }
        return selfScore;
    }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }

    public String getSelfFeedback() {
        if (selfFeedback != null && !selfFeedback.isBlank()) {
            return selfFeedback;
        }
        StringBuilder sb = new StringBuilder();
        if (comments != null && !comments.isBlank()) {
            sb.append(comments.trim());
        }
        if (achievements != null && !achievements.isBlank()) {
            if (!sb.isEmpty()) sb.append(". ");
            sb.append("Achievements: ").append(achievements.trim());
        }
        if (developmentAreas != null && !developmentAreas.isBlank()) {
            if (!sb.isEmpty()) sb.append(". ");
            sb.append("Development Areas: ").append(developmentAreas.trim());
        }
        return sb.isEmpty() ? "Self review completed" : sb.toString();
    }
    public void setSelfFeedback(String selfFeedback) { this.selfFeedback = selfFeedback; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getDevelopmentAreas() { return developmentAreas; }
    public void setDevelopmentAreas(String developmentAreas) { this.developmentAreas = developmentAreas; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
