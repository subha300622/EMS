package com.example.ems.performance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceSnapshotDto {

    private EmployeeSnapshotItem employee = new EmployeeSnapshotItem();
    private CycleSnapshotItem cycle = new CycleSnapshotItem();
    private AttendanceSnapshotItem attendance = new AttendanceSnapshotItem();
    private LeaveSnapshotItem leave = new LeaveSnapshotItem();
    private List<GoalSnapshotItem> goals = new ArrayList<>();
    private List<KpiSnapshotItem> kpis = new ArrayList<>();

    public PerformanceSnapshotDto() {}

    public EmployeeSnapshotItem getEmployee() { return employee; }
    public void setEmployee(EmployeeSnapshotItem employee) { this.employee = employee; }

    public CycleSnapshotItem getCycle() { return cycle; }
    public void setCycle(CycleSnapshotItem cycle) { this.cycle = cycle; }

    public AttendanceSnapshotItem getAttendance() { return attendance; }
    public void setAttendance(AttendanceSnapshotItem attendance) { this.attendance = attendance; }

    public LeaveSnapshotItem getLeave() { return leave; }
    public void setLeave(LeaveSnapshotItem leave) { this.leave = leave; }

    public List<GoalSnapshotItem> getGoals() { return goals; }
    public void setGoals(List<GoalSnapshotItem> goals) { this.goals = goals; }

    public List<KpiSnapshotItem> getKpis() { return kpis; }
    public void setKpis(List<KpiSnapshotItem> kpis) { this.kpis = kpis; }

    public static class EmployeeSnapshotItem {
        private Long employeeId;
        private String employeeCode;
        private String name;
        private String department;
        private String designation;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
    }

    public static class CycleSnapshotItem {
        private Long cycleId;
        private String code;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer calculationVersion;
        private String formulaVersion;

        public Long getCycleId() { return cycleId; }
        public void setCycleId(Long cycleId) { this.cycleId = cycleId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Integer getCalculationVersion() { return calculationVersion; }
        public void setCalculationVersion(Integer calculationVersion) { this.calculationVersion = calculationVersion; }
        public String getFormulaVersion() { return formulaVersion; }
        public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }
    }

    public static class AttendanceSnapshotItem {
        private String sourceVersion = "attendance-v1";
        private Integer workingDays = 0;
        private Integer presentDays = 0;
        private BigDecimal attendancePercentage = new BigDecimal("100.00");
        private BigDecimal attendanceScore = new BigDecimal("100.00");

        public String getSourceVersion() { return sourceVersion; }
        public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
        public Integer getWorkingDays() { return workingDays; }
        public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }
        public Integer getPresentDays() { return presentDays; }
        public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }
        public BigDecimal getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }
        public BigDecimal getAttendanceScore() { return attendanceScore; }
        public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }
    }

    public static class LeaveSnapshotItem {
        private String sourceVersion = "leave-v1";
        private Integer totalLeavesTaken = 0;
        private Integer unpaidLeaves = 0;

        public String getSourceVersion() { return sourceVersion; }
        public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
        public Integer getTotalLeavesTaken() { return totalLeavesTaken; }
        public void setTotalLeavesTaken(Integer totalLeavesTaken) { this.totalLeavesTaken = totalLeavesTaken; }
        public Integer getUnpaidLeaves() { return unpaidLeaves; }
        public void setUnpaidLeaves(Integer unpaidLeaves) { this.unpaidLeaves = unpaidLeaves; }
    }

    public static class GoalSnapshotItem {
        private Long goalId;
        private String title;
        private Integer progressPercent = 0;
        private String status;

        public Long getGoalId() { return goalId; }
        public void setGoalId(Long goalId) { this.goalId = goalId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getProgressPercent() { return progressPercent; }
        public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class KpiSnapshotItem {
        private String kpiCode;
        private String measurementType;
        private BigDecimal weight;
        private BigDecimal targetValue;
        private BigDecimal actualValue;
        private BigDecimal normalizedScore;

        public String getKpiCode() { return kpiCode; }
        public void setKpiCode(String kpiCode) { this.kpiCode = kpiCode; }
        public String getMeasurementType() { return measurementType; }
        public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getTargetValue() { return targetValue; }
        public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }
        public BigDecimal getActualValue() { return actualValue; }
        public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
        public BigDecimal getNormalizedScore() { return normalizedScore; }
        public void setNormalizedScore(BigDecimal normalizedScore) { this.normalizedScore = normalizedScore; }
    }
}
