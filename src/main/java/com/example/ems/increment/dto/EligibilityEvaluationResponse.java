package com.example.ems.increment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Eligibility Evaluation Response")
public class EligibilityEvaluationResponse {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;
    @Schema(description = "Appraisal ID", example = "5")
    private Long appraisalId;
    @Schema(description = "Is employee eligible for increment", example = "true")
    private boolean eligible;
    @Schema(description = "Final appraisal rating score", example = "4.2")
    private Double finalRating;
    @Schema(description = "Goal achievement percentage", example = "90.0")
    private Double goalAchievementPercentage;
    @Schema(description = "Attendance percentage", example = "95.0")
    private Double attendancePercentage;
    @Schema(description = "Is disciplinary record clear", example = "true")
    private boolean disciplinaryClear;
    @Schema(description = "Is budget available", example = "true")
    private boolean budgetAvailable;
    @Schema(description = "Eligibility summary reason", example = "Eligible based on policy criteria")
    private String reason;
    @Schema(description = "List of specific ineligibility reasons if any")
    private List<IneligibleReasonDto> reasons;

    public static class IneligibleReasonDto {
        private String rule;
        private Object required;
        private Object actual;
        private String message;

        public IneligibleReasonDto() {}

        public IneligibleReasonDto(String rule, Object required, Object actual, String message) {
            this.rule = rule;
            this.required = required;
            this.actual = actual;
            this.message = message;
        }

        public String getRule() { return rule; }
        public void setRule(String rule) { this.rule = rule; }

        public Object getRequired() { return required; }
        public void setRequired(Object required) { this.required = required; }

        public Object getActual() { return actual; }
        public void setActual(Object actual) { this.actual = actual; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public Double getGoalAchievementPercentage() { return goalAchievementPercentage; }
    public void setGoalAchievementPercentage(Double goalAchievementPercentage) { this.goalAchievementPercentage = goalAchievementPercentage; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public boolean isDisciplinaryClear() { return disciplinaryClear; }
    public void setDisciplinaryClear(boolean disciplinaryClear) { this.disciplinaryClear = disciplinaryClear; }

    public boolean isBudgetAvailable() { return budgetAvailable; }
    public void setBudgetAvailable(boolean budgetAvailable) { this.budgetAvailable = budgetAvailable; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<IneligibleReasonDto> getReasons() { return reasons; }
    public void setReasons(List<IneligibleReasonDto> reasons) { this.reasons = reasons; }
}
