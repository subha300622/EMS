package com.example.ems.onboarding.dto.selfservice;

import java.util.List;

public class OnboardingProgressResponse {

    private Long onboardingId;
    private double overallProgress;
    private List<PhaseProgressItem> phases;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public double getOverallProgress() { return overallProgress; }
    public void setOverallProgress(double overallProgress) { this.overallProgress = overallProgress; }

    public List<PhaseProgressItem> getPhases() { return phases; }
    public void setPhases(List<PhaseProgressItem> phases) { this.phases = phases; }

    public static class PhaseProgressItem {
        private Long phaseId;
        private String phaseName;
        private double progress;

        public PhaseProgressItem(Long phaseId, String phaseName, double progress) {
            this.phaseId = phaseId;
            this.phaseName = phaseName;
            this.progress = progress;
        }

        public Long getPhaseId() { return phaseId; }
        public void setPhaseId(Long phaseId) { this.phaseId = phaseId; }

        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }

        public double getProgress() { return progress; }
        public void setProgress(double progress) { this.progress = progress; }
    }
}
