package com.example.ems.onboarding.dto;

import java.time.LocalDate;

public class OnboardingUpdateRequest {

    private LocalDate joiningDate;
    private String reportingManager; // could be manager's full name or ID
    private String status;

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getReportingManager() { return reportingManager; }
    public void setReportingManager(String reportingManager) { this.reportingManager = reportingManager; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
