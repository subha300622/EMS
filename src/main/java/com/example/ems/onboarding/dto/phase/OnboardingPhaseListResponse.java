package com.example.ems.onboarding.dto.phase;

import java.util.List;

public class OnboardingPhaseListResponse {

    private Long onboardingId;
    private List<PhaseItem> phases;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public List<PhaseItem> getPhases() { return phases; }
    public void setPhases(List<PhaseItem> phases) { this.phases = phases; }

    public static class PhaseItem {
        private Long phaseId;
        private String name;
        private String status;
        private int totalTasks;
        private int completedTasks;
        private double progressPercentage;

        public Long getPhaseId() { return phaseId; }
        public void setPhaseId(Long phaseId) { this.phaseId = phaseId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    }
}
