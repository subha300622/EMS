package com.example.ems.onboarding.event;

import com.example.ems.onboarding.service.TeamOnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OnboardingPhaseUpdaterListener {

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @EventListener
    @Order(1)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        teamOnboardingService.updateProgressOnTaskCompletion(event.getOnboardingId(), event.getTaskId(), event.getPhaseName());
    }
}
