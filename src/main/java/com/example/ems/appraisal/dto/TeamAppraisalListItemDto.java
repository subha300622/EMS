package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.AppraisalStatus;
import java.math.BigDecimal;

public class TeamAppraisalListItemDto {
    private Long appraisalId;
    private Long employeeId;
    private String employeeRefId;
    private String employeeName;
    private String employeeDesignation;
    private String employeeProfileImage;
    private String department;
    private double attendance;
    private int leaves;
    private int late;
    private double perfScore;
    private double kpiScore;
    private Double managerRating;
    private BigDecimal incrementPercentage;
    private BigDecimal currentSalary;
    private BigDecimal revisedSalary;
    private AppraisalStatus status;
    private boolean manualReviewRequired;
    private String reviewReason;
    private Long incrementId;
    private Long cycleId;
    private String cycleName;
    private Integer appraisalYear;

    public TeamAppraisalListItemDto() {}

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeRefId() { return employeeRefId; }
    public void setEmployeeRefId(String employeeRefId) { this.employeeRefId = employeeRefId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeDesignation() { return employeeDesignation; }
    public void setEmployeeDesignation(String employeeDesignation) { this.employeeDesignation = employeeDesignation; }

    public String getEmployeeProfileImage() { return employeeProfileImage; }
    public void setEmployeeProfileImage(String employeeProfileImage) { this.employeeProfileImage = employeeProfileImage; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public double getAttendance() { return attendance; }
    public void setAttendance(double attendance) { this.attendance = attendance; }

    public int getLeaves() { return leaves; }
    public void setLeaves(int leaves) { this.leaves = leaves; }

    public int getLate() { return late; }
    public void setLate(int late) { this.late = late; }

    public double getPerfScore() { return perfScore; }
    public void setPerfScore(double perfScore) { this.perfScore = perfScore; }

    public double getKpiScore() { return kpiScore; }
    public void setKpiScore(double kpiScore) { this.kpiScore = kpiScore; }

    public Double getManagerRating() { return managerRating; }
    public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public BigDecimal getRevisedSalary() { return revisedSalary; }
    public void setRevisedSalary(BigDecimal revisedSalary) { this.revisedSalary = revisedSalary; }

    public AppraisalStatus getStatus() { return status; }
    public void setStatus(AppraisalStatus status) { this.status = status; }

    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }

    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) { this.reviewReason = reviewReason; }

    public Long getIncrementId() { return incrementId; }
    public void setIncrementId(Long incrementId) { this.incrementId = incrementId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public Integer getAppraisalYear() { return appraisalYear; }
    public void setAppraisalYear(Integer appraisalYear) { this.appraisalYear = appraisalYear; }
}
