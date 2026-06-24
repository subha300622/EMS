package com.example.ems.onboarding.event;

import com.example.ems.onboarding.entity.OnboardingEventLog;
import com.example.ems.onboarding.repository.OnboardingEventLogRepository;
import com.example.ems.finance.event.FinanceCommandExecutedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class OnboardingLifecycleListener {

    @Autowired
    private OnboardingEventLogRepository eventLogRepository;

    @EventListener
    @Transactional
    public void handleFinanceCommandExecuted(FinanceCommandExecutedEvent event) {
        OnboardingEventLog log = new OnboardingEventLog();
        log.setOnboardingId(event.getOnboardingId());
        log.setEventType("FINANCE_COMMAND_" + event.getAction());
        log.setEventData("Command processed: " + event.getAction());
        log.setStatus("SUCCESS".equalsIgnoreCase(event.getStatus()) ? "SUCCESS" : "FAILED");
        log.setErrorMessage(event.getErrorMessage());
        log.setCommandId(event.getCommandId());
        log.setCorrelationId(event.getCorrelationId());
        log.setCausationId(event.getCausationId());
        log.setAggregateVersion(event.getAggregateVersion());
        log.setCommandStatus(event.getStatus());
        log.setExecutionTimeMs(event.getExecutionTimeMs());
        log.setTimestamp(LocalDateTime.now());
        
        eventLogRepository.save(log);
    }
}
