package com.example.ems.performance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Performance Review Score Calculation Request Parameters")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceCalculationRequest {

    @Schema(description = "Itemized KPI evaluation score inputs")
    private List<KpiScoreInputDto> kpiScores = new ArrayList<>();

    @Schema(description = "Manager evaluation score (1.00 - 5.00)", example = "4.30")
    private BigDecimal managerScore;

    @Schema(description = "Manager qualitative feedback", example = "Consistently meets project goals with high code quality.")
    private String managerFeedback;

    @Schema(description = "Total leaves taken in review cycle", example = "2")
    private Integer leavesTaken;

    @Schema(description = "Attendance percentage (0 - 100)", example = "97.50")
    private BigDecimal attendancePercentage;

    @Schema(description = "Idempotency key to safely retry calculation", example = "calc-run-9a8b7c")
    private String idempotencyKey;

    public PerformanceCalculationRequest() {}

    public List<KpiScoreInputDto> getKpiScores() { return kpiScores; }
    public void setKpiScores(List<KpiScoreInputDto> kpiScores) { this.kpiScores = kpiScores; }

    public BigDecimal getManagerScore() { return managerScore; }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public String getManagerFeedback() { return managerFeedback; }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }

    public Integer getLeavesTaken() { return leavesTaken; }
    public void setLeavesTaken(Integer leavesTaken) { this.leavesTaken = leavesTaken; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    @Schema(description = "Individual KPI Score Input")
    public static class KpiScoreInputDto {
        @Schema(description = "KPI definition ID", example = "51")
        private Long kpiId;

        @Schema(description = "Achieved actual measurement value", example = "95.00")
        private BigDecimal actualValue;

        @Schema(description = "Evaluation commentary", example = "Completed all sprint objectives.")
        private String comments;

        public KpiScoreInputDto() {}

        public KpiScoreInputDto(Long kpiId, BigDecimal actualValue, String comments) {
            this.kpiId = kpiId;
            this.actualValue = actualValue;
            this.comments = comments;
        }

        public Long getKpiId() { return kpiId; }
        public void setKpiId(Long kpiId) { this.kpiId = kpiId; }
        public BigDecimal getActualValue() { return actualValue; }
        public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}

