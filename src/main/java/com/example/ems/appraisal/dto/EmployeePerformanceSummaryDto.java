package com.example.ems.appraisal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeePerformanceSummaryDto {

    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String department;
    private String designation;

    private List<PreviousAppraisalDto> previousAppraisals = new ArrayList<>();
    private CurrentPeriodPerformanceDto currentPeriodPerformance;
    private ReviewContextDto reviewContext;

    public EmployeePerformanceSummaryDto() {}

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<PreviousAppraisalDto> getPreviousAppraisals() {
        return previousAppraisals;
    }

    public void setPreviousAppraisals(List<PreviousAppraisalDto> previousAppraisals) {
        this.previousAppraisals = previousAppraisals;
    }

    public CurrentPeriodPerformanceDto getCurrentPeriodPerformance() {
        return currentPeriodPerformance;
    }

    public void setCurrentPeriodPerformance(CurrentPeriodPerformanceDto currentPeriodPerformance) {
        this.currentPeriodPerformance = currentPeriodPerformance;
    }

    public ReviewContextDto getReviewContext() {
        return reviewContext;
    }

    public void setReviewContext(ReviewContextDto reviewContext) {
        this.reviewContext = reviewContext;
    }

    public static class PreviousAppraisalDto {
        private Long appraisalId;
        private String cycle;
        private Integer cycleYear;
        private Double finalScore;
        private Double maxScore;
        private String scoreScale;
        private String rating;
        private LocalDateTime completedAt;

        public PreviousAppraisalDto() {}

        public PreviousAppraisalDto(Long appraisalId, String cycle, Integer cycleYear, Double finalScore, String rating, LocalDateTime completedAt) {
            this(appraisalId, cycle, cycleYear, finalScore, 5.0, "5_POINT", rating, completedAt);
        }

        public PreviousAppraisalDto(Long appraisalId, String cycle, Integer cycleYear, Double finalScore, Double maxScore, String scoreScale, String rating, LocalDateTime completedAt) {
            this.appraisalId = appraisalId;
            this.cycle = cycle;
            this.cycleYear = cycleYear;
            this.finalScore = finalScore;
            this.maxScore = maxScore;
            this.scoreScale = scoreScale;
            this.rating = rating;
            this.completedAt = completedAt;
        }

        public Long getAppraisalId() {
            return appraisalId;
        }

        public void setAppraisalId(Long appraisalId) {
            this.appraisalId = appraisalId;
        }

        public String getCycle() {
            return cycle;
        }

        public void setCycle(String cycle) {
            this.cycle = cycle;
        }

        public Integer getCycleYear() {
            return cycleYear;
        }

        public void setCycleYear(Integer cycleYear) {
            this.cycleYear = cycleYear;
        }

        public Double getFinalScore() {
            return finalScore;
        }

        public void setFinalScore(Double finalScore) {
            this.finalScore = finalScore;
        }

        public Double getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(Double maxScore) {
            this.maxScore = maxScore;
        }

        public String getScoreScale() {
            return scoreScale;
        }

        public void setScoreScale(String scoreScale) {
            this.scoreScale = scoreScale;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }
    }

    public static class CurrentPeriodPerformanceDto {
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Double attendancePercentage;
        private Integer workingDays;
        private Integer presentDays;
        private Integer leaveDays;
        private Integer totalGoals;
        private Integer completedGoals;
        private Double goalCompletionPercentage;
        private Double kpiAchievementPercentage;

        public CurrentPeriodPerformanceDto() {}

        public LocalDate getPeriodStart() {
            return periodStart;
        }

        public void setPeriodStart(LocalDate periodStart) {
            this.periodStart = periodStart;
        }

        public LocalDate getPeriodEnd() {
            return periodEnd;
        }

        public void setPeriodEnd(LocalDate periodEnd) {
            this.periodEnd = periodEnd;
        }

        public Double getAttendancePercentage() {
            return attendancePercentage;
        }

        public void setAttendancePercentage(Double attendancePercentage) {
            this.attendancePercentage = attendancePercentage;
        }

        public Integer getWorkingDays() {
            return workingDays;
        }

        public void setWorkingDays(Integer workingDays) {
            this.workingDays = workingDays;
        }

        public Integer getPresentDays() {
            return presentDays;
        }

        public void setPresentDays(Integer presentDays) {
            this.presentDays = presentDays;
        }

        public Integer getLeaveDays() {
            return leaveDays;
        }

        public void setLeaveDays(Integer leaveDays) {
            this.leaveDays = leaveDays;
        }

        public Integer getTotalGoals() {
            return totalGoals;
        }

        public void setTotalGoals(Integer totalGoals) {
            this.totalGoals = totalGoals;
        }

        public Integer getCompletedGoals() {
            return completedGoals;
        }

        public void setCompletedGoals(Integer completedGoals) {
            this.completedGoals = completedGoals;
        }

        public Double getGoalCompletionPercentage() {
            return goalCompletionPercentage;
        }

        public void setGoalCompletionPercentage(Double goalCompletionPercentage) {
            this.goalCompletionPercentage = goalCompletionPercentage;
        }

        public Double getKpiAchievementPercentage() {
            return kpiAchievementPercentage;
        }

        public void setKpiAchievementPercentage(Double kpiAchievementPercentage) {
            this.kpiAchievementPercentage = kpiAchievementPercentage;
        }
    }

    public static class ReviewContextDto {
        private boolean previousReviewAvailable;
        private Long currentReviewId;
        private String currentCycleName;
        private Integer currentStageOrder;
        private String appraisalStatus;

        public ReviewContextDto() {}

        public boolean isPreviousReviewAvailable() {
            return previousReviewAvailable;
        }

        public void setPreviousReviewAvailable(boolean previousReviewAvailable) {
            this.previousReviewAvailable = previousReviewAvailable;
        }

        public Long getCurrentReviewId() {
            return currentReviewId;
        }

        public void setCurrentReviewId(Long currentReviewId) {
            this.currentReviewId = currentReviewId;
        }

        public String getCurrentCycleName() {
            return currentCycleName;
        }

        public void setCurrentCycleName(String currentCycleName) {
            this.currentCycleName = currentCycleName;
        }

        public Integer getCurrentStageOrder() {
            return currentStageOrder;
        }

        public void setCurrentStageOrder(Integer currentStageOrder) {
            this.currentStageOrder = currentStageOrder;
        }

        public String getAppraisalStatus() {
            return appraisalStatus;
        }

        public void setAppraisalStatus(String appraisalStatus) {
            this.appraisalStatus = appraisalStatus;
        }
    }
}
