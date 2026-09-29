package com.example.ems.onboarding.dto;

import java.time.LocalDate;
import java.util.Map;

public class OnboardingLaunchRequest {

    private String employeeId;
    private String employeeName;
    private String email;
    private LocalDate joiningDate;
    private String department;
    private String designation;
    private String reportingManager;
    private String employmentType;
    private String templateId;
    private TeamAssignments teamAssignments;
    private Map<String, Boolean> notifications;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getReportingManager() { return reportingManager; }
    public void setReportingManager(String reportingManager) { this.reportingManager = reportingManager; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public TeamAssignments getTeamAssignments() { return teamAssignments; }
    public void setTeamAssignments(TeamAssignments teamAssignments) { this.teamAssignments = teamAssignments; }

    public Map<String, Boolean> getNotifications() { return notifications; }
    public void setNotifications(Map<String, Boolean> notifications) { this.notifications = notifications; }

    public static class TeamAssignments {
        private String hrOwnerId;
        private String buddyId;
        private String itContactId;
        private String financeContactId;

        public String getHrOwnerId() { return hrOwnerId; }
        public void setHrOwnerId(String hrOwnerId) { this.hrOwnerId = hrOwnerId; }

        public String getBuddyId() { return buddyId; }
        public void setBuddyId(String buddyId) { this.buddyId = buddyId; }

        public String getItContactId() { return itContactId; }
        public void setItContactId(String itContactId) { this.itContactId = itContactId; }

        public String getFinanceContactId() { return financeContactId; }
        public void setFinanceContactId(String financeContactId) { this.financeContactId = financeContactId; }
    }
}
