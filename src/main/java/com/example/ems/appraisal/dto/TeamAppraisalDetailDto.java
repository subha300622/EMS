package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.AppraisalStatus;
import java.math.BigDecimal;
import java.util.List;

public class TeamAppraisalDetailDto {
    private Long appraisalId;
    private EmployeeInfo employee;
    private double attendance;
    private double perfScore;
    private Double managerRating;
    private BigDecimal incrementPercentage;
    private BigDecimal currentSalary;
    private BigDecimal revisedSalary;
    private BigDecimal annualIncreaseImpact;
    private int goalsCompleted;
    private int totalGoals;
    private double kpiScore;
    private double peerRating;
    private Long cycleId;
    private String cycleName;
    private Integer appraisalYear;
    private List<JourneyStep> journey;
    private AppraisalStatus status;

    public TeamAppraisalDetailDto() {}

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public double getAttendance() { return attendance; }
    public void setAttendance(double attendance) { this.attendance = attendance; }

    public double getPerfScore() { return perfScore; }
    public void setPerfScore(double perfScore) { this.perfScore = perfScore; }

    public Double getManagerRating() { return managerRating; }
    public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public BigDecimal getRevisedSalary() { return revisedSalary; }
    public void setRevisedSalary(BigDecimal revisedSalary) { this.revisedSalary = revisedSalary; }

    public BigDecimal getAnnualIncreaseImpact() { return annualIncreaseImpact; }
    public void setAnnualIncreaseImpact(BigDecimal annualIncreaseImpact) { this.annualIncreaseImpact = annualIncreaseImpact; }

    public int getGoalsCompleted() { return goalsCompleted; }
    public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }

    public int getTotalGoals() { return totalGoals; }
    public void setTotalGoals(int totalGoals) { this.totalGoals = totalGoals; }

    public double getKpiScore() { return kpiScore; }
    public void setKpiScore(double kpiScore) { this.kpiScore = kpiScore; }

    public double getPeerRating() { return peerRating; }
    public void setPeerRating(double peerRating) { this.peerRating = peerRating; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public Integer getAppraisalYear() { return appraisalYear; }
    public void setAppraisalYear(Integer appraisalYear) { this.appraisalYear = appraisalYear; }

    public List<JourneyStep> getJourney() { return journey; }
    public void setJourney(List<JourneyStep> journey) { this.journey = journey; }

    public AppraisalStatus getStatus() { return status; }
    public void setStatus(AppraisalStatus status) { this.status = status; }

    public static class EmployeeInfo {
        private Long id;
        private String employeeId;
        private String name;
        private String role;
        private String department;
        private String profileImage;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String employeeId, String name, String role, String department, String profileImage) {
            this.id = id;
            this.employeeId = employeeId;
            this.name = name;
            this.role = role;
            this.department = department;
            this.profileImage = profileImage;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    }

    public static class JourneyStep {
        private String stepName;
        private String status;
        private String description;

        public JourneyStep() {}

        public JourneyStep(String stepName, String status, String description) {
            this.stepName = stepName;
            this.status = status;
            this.description = description;
        }

        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
