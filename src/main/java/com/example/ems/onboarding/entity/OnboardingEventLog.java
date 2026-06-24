package com.example.ems.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_event_logs")
public class OnboardingEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long onboardingId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String eventData;

    @Column(nullable = false)
    private String status = "SUCCESS"; // SUCCESS, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer retryCount = 0;

    private String failureCategory; // e.g. INTEGRATION, DATABASE, VALIDATION

    private Boolean replayFlag = false;

    // CQRS Tracking & Observability
    private String commandId;
    private String correlationId;
    private String causationId;
    private Long aggregateVersion;
    private String commandStatus; // SUCCESS, FAILED, REJECTED
    private Long executionTimeMs;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public OnboardingEventLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }

    public Boolean getReplayFlag() { return replayFlag; }
    public void setReplayFlag(Boolean replayFlag) { this.replayFlag = replayFlag; }

    public String getCommandId() { return commandId; }
    public void setCommandId(String commandId) { this.commandId = commandId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }

    public Long getAggregateVersion() { return aggregateVersion; }
    public void setAggregateVersion(Long aggregateVersion) { this.aggregateVersion = aggregateVersion; }

    public String getCommandStatus() { return commandStatus; }
    public void setCommandStatus(String commandStatus) { this.commandStatus = commandStatus; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
