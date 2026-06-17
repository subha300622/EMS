package com.example.ems.employee.dto;

public class EmployeeAvailabilityResponse {
    private Long employeeId;
    private String availability;
    private String currentStatus;
    private String lastActiveAt;

    public EmployeeAvailabilityResponse() {}

    public EmployeeAvailabilityResponse(Long employeeId, String availability, String currentStatus, String lastActiveAt) {
        this.employeeId = employeeId;
        this.availability = availability;
        this.currentStatus = currentStatus;
        this.lastActiveAt = lastActiveAt;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
