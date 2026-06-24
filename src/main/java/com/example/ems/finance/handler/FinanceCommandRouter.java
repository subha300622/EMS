package com.example.ems.finance.handler;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.finance.event.FinanceCommandExecutedEvent;
import com.example.ems.common.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FinanceCommandRouter {

    @Autowired
    private BankCommandHandler bankCommandHandler;

    @Autowired
    private TaxCommandHandler taxCommandHandler;

    @Autowired
    private StatutoryCommandHandler statutoryCommandHandler;

    @Autowired
    private SalaryCommandHandler salaryCommandHandler;

    @Autowired
    private LifecycleCommandHandler lifecycleCommandHandler;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public EmployeeFinanceOnboarding route(Long onboardingId, FinanceCommandEnvelope command, String userEmail, String headerIdempotencyKey) {
        // Enforce idempotency check in the router layer
        String idempotencyKey = headerIdempotencyKey != null ? headerIdempotencyKey : 
                (command.getMetadata() != null ? command.getMetadata().getIdempotencyKey() : null);

        String correlationId = UUID.randomUUID().toString();
        String causationId = correlationId; // root command

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String scopedKey = String.format("%s:%d:FINANCE:%s", userEmail, onboardingId, idempotencyKey);
            if (idempotencyService.isDuplicate(scopedKey)) {
                // If it is a duplicate, return a mock success state or throw exception
                throw new IllegalStateException("Duplicate command detected for key: " + idempotencyKey);
            }
        }

        String action = command.getAction().toUpperCase();
        long startTime = System.currentTimeMillis();
        EmployeeFinanceOnboarding result = null;
        String status = "SUCCESS";
        String errorMessage = null;

        try {
            switch (action) {
                case "UPDATE_BANK":
                    result = bankCommandHandler.handle(onboardingId, command, userEmail);
                    break;
                case "UPDATE_TAX":
                    result = taxCommandHandler.handle(onboardingId, command, userEmail);
                    break;
                case "UPDATE_STATUTORY":
                    result = statutoryCommandHandler.handle(onboardingId, command, userEmail);
                    break;
                case "ASSIGN_SALARY":
                    result = salaryCommandHandler.handle(onboardingId, command, userEmail);
                    break;
                case "SUBMIT":
                case "APPROVE":
                case "REJECT":
                case "REINITIALIZE":
                case "ACTIVATE":
                    result = lifecycleCommandHandler.handle(onboardingId, command, userEmail);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported command action: " + action);
            }
        } catch (Exception e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Long optVersion = result != null ? (result.getOptVersion() != null ? result.getOptVersion().longValue() : 0L) : 0L;
            
            // Publish the projection log update event
            eventPublisher.publishEvent(new FinanceCommandExecutedEvent(
                    this,
                    onboardingId,
                    action,
                    idempotencyKey,
                    correlationId,
                    causationId,
                    status,
                    errorMessage,
                    duration,
                    optVersion
            ));
        }

        return result;
    }
}
