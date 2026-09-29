package com.example.ems.performance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Manager Performance Review Evaluation Submission Payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseManagerReviewRequest {

    @Schema(description = "Manager evaluation rating (1.00 - 5.00)", example = "4.30")
    private BigDecimal managerScore;

    @Schema(description = "Manager rating input alternative", example = "4")
    private Number managerRating;

    @Schema(description = "Manager evaluation feedback and development recommendations", example = "Outstanding technical leadership.")
    private String managerFeedback;

    @Schema(description = "General manager comments", example = "Manager review completed")
    private String comments;

    @Schema(description = "Employee strengths noted by manager", example = "Strong technical delivery")
    private String strengths;

    @Schema(description = "Areas for employee growth and development", example = "Improve cross-team communication")
    private String developmentAreas;

    @Schema(description = "Idempotency key to ensure safe submission retry", example = "8d0b23c4-5678")
    private String idempotencyKey;

    public EnterpriseManagerReviewRequest() {}

    public EnterpriseManagerReviewRequest(BigDecimal managerScore, String managerFeedback) {
        this.managerScore = managerScore;
        this.managerFeedback = managerFeedback;
    }

    public BigDecimal getManagerScore() {
        if (managerScore != null) {
            return managerScore;
        }
        if (managerRating != null) {
            return BigDecimal.valueOf(managerRating.doubleValue());
        }
        return BigDecimal.valueOf(4.00);
    }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public Number getManagerRating() { return managerRating; }
    public void setManagerRating(Number managerRating) {
        this.managerRating = managerRating;
        if (managerRating != null) {
            this.managerScore = BigDecimal.valueOf(managerRating.doubleValue());
        }
    }

    public String getManagerFeedback() {
        if (managerFeedback != null && !managerFeedback.isBlank()) {
            return managerFeedback;
        }
        StringBuilder sb = new StringBuilder();
        if (comments != null && !comments.isBlank()) {
            sb.append(comments.trim());
        }
        if (strengths != null && !strengths.isBlank()) {
            if (!sb.isEmpty()) sb.append(". ");
            sb.append("Strengths: ").append(strengths.trim());
        }
        if (developmentAreas != null && !developmentAreas.isBlank()) {
            if (!sb.isEmpty()) sb.append(". ");
            sb.append("Development Areas: ").append(developmentAreas.trim());
        }
        return sb.isEmpty() ? "Manager review completed" : sb.toString();
    }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getDevelopmentAreas() { return developmentAreas; }
    public void setDevelopmentAreas(String developmentAreas) { this.developmentAreas = developmentAreas; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
