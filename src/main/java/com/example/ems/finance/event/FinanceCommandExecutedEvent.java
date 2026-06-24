package com.example.ems.finance.event;

import org.springframework.context.ApplicationEvent;

public class FinanceCommandExecutedEvent extends ApplicationEvent {
    private final Long onboardingId;
    private final String action;
    private final String commandId;
    private final String correlationId;
    private final String causationId;
    private final String status;
    private final String errorMessage;
    private final Long executionTimeMs;
    private final Long aggregateVersion;

    public FinanceCommandExecutedEvent(Object source, Long onboardingId, String action, String commandId,
                                       String correlationId, String causationId, String status,
                                       String errorMessage, Long executionTimeMs, Long aggregateVersion) {
        super(source);
        this.onboardingId = onboardingId;
        this.action = action;
        this.commandId = commandId;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.aggregateVersion = aggregateVersion;
    }

    public Long getOnboardingId() { return onboardingId; }
    public String getAction() { return action; }
    public String getCommandId() { return commandId; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getAggregateVersion() { return aggregateVersion; }
}
