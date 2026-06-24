package com.example.ems.onboarding.event;

import org.springframework.context.ApplicationEvent;

public class OnboardingCompletedEvent extends ApplicationEvent {
    private final Long onboardingId;

    public OnboardingCompletedEvent(Object source, Long onboardingId) {
        super(source);
        this.onboardingId = onboardingId;
    }

    public Long getOnboardingId() {
        return onboardingId;
    }
}
