package com.example.ems.security.service;

import com.example.ems.security.dto.AuthDecisionTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SecurityTelemetryPublisher {

    private static final Logger log = LoggerFactory.getLogger(SecurityTelemetryPublisher.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public enum TelemetryEvent {
        LOGIN_SUCCESS,
        LOGIN_FAIL,
        SESSION_REVOKED,
        TOKEN_REJECTED
    }

    public static class SecurityTelemetryEvent {
        private final TelemetryEvent type;
        private final AuthDecisionTrace trace;

        public SecurityTelemetryEvent(TelemetryEvent type, AuthDecisionTrace trace) {
            this.type = type;
            this.trace = trace;
        }

        public TelemetryEvent getType() { return type; }
        public AuthDecisionTrace getTrace() { return trace; }

        @Override
        public String toString() {
            return "SecurityTelemetryEvent{" +
                    "type=" + type +
                    ", trace=" + trace +
                    '}';
        }
    }

    public void publish(TelemetryEvent eventType, AuthDecisionTrace trace) {
        log.info("SECURITY_TELEMETRY: Event={}, Trace={}", eventType, trace);
        try {
            eventPublisher.publishEvent(new SecurityTelemetryEvent(eventType, trace));
        } catch (Exception e) {
            log.error("Failed to publish security telemetry event: {}", e.getMessage());
        }
    }
}
