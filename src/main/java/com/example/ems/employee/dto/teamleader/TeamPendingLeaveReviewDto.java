package com.example.ems.employee.dto.teamleader;

public class TeamPendingLeaveReviewDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private String startDate;
    private String endDate;
    private Double durationDays;
    private String reason;
    private String status;
    private String appliedAt;

    public TeamPendingLeaveReviewDto() {}

    public TeamPendingLeaveReviewDto(Long id, Long employeeId, String employeeName, String leaveType, String startDate, String endDate, Double durationDays, String reason, String status, String appliedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationDays = durationDays;
        this.reason = reason;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Double getDurationDays() { return durationDays; }
    public void setDurationDays(Double durationDays) { this.durationDays = durationDays; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }
}
