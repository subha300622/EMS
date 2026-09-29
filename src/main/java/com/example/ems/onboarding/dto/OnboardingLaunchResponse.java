package com.example.ems.onboarding.dto;

import java.time.LocalDate;

public class OnboardingLaunchResponse {

    private OnboardingInfo onboarding;
    private int phasesCreated;
    private int tasksCreated;
    private int documentsCreated;

    public OnboardingInfo getOnboarding() { return onboarding; }
    public void setOnboarding(OnboardingInfo onboarding) { this.onboarding = onboarding; }

    public int getPhasesCreated() { return phasesCreated; }
    public void setPhasesCreated(int phasesCreated) { this.phasesCreated = phasesCreated; }

    public int getTasksCreated() { return tasksCreated; }
    public void setTasksCreated(int tasksCreated) { this.tasksCreated = tasksCreated; }

    public int getDocumentsCreated() { return documentsCreated; }
    public void setDocumentsCreated(int documentsCreated) { this.documentsCreated = documentsCreated; }

    public static class OnboardingInfo {
        private String id;
        private String employeeId;
        private String assignedTemplateId;
        private String status;
        private int progress;
        private LocalDate joiningDate;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getAssignedTemplateId() { return assignedTemplateId; }
        public void setAssignedTemplateId(String assignedTemplateId) { this.assignedTemplateId = assignedTemplateId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }

        public LocalDate getJoiningDate() { return joiningDate; }
        public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    }
}
