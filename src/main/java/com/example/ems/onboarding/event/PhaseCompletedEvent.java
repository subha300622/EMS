package com.example.ems.onboarding.event;

import org.springframework.context.ApplicationEvent;

public class PhaseCompletedEvent extends ApplicationEvent {
    private final Long onboardingId;
    private final String phaseName;

    public PhaseCompletedEvent(Object source, Long onboardingId, String phaseName) {
        super(source);
        this.onboardingId = onboardingId;
        this.phaseName = phaseName;
    }

    public Long getOnboardingId() {
        return onboardingId;
    }

    public String getPhaseName() {
        return phaseName;
    }
}
