package com.example.ems.common.kafka;

import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.event.DomainEventPublisher;
import com.example.ems.common.event.EventEnvelope;
import com.example.ems.common.event.EventTypes;
import com.example.ems.common.event.NotificationCreatedEvent;
import com.example.ems.security.context.SecurityContextFacade;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Developer-only test API to verify the DomainEventPublisher -> Outbox -> Scheduler -> Kafka -> Consumer pipeline.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v1/admin/test")
public class OutboxTestController {

    private final DomainEventPublisher domainEventPublisher;
    private final SecurityContextFacade securityContextFacade;
    private final RoleService roleService;

    public OutboxTestController(DomainEventPublisher domainEventPublisher,
                                SecurityContextFacade securityContextFacade,
                                RoleService roleService) {
        this.domainEventPublisher = domainEventPublisher;
        this.securityContextFacade = securityContextFacade;
        this.roleService = roleService;
    }

    /**
     * Publishes a test notification created event to verify the outbox pipeline.
     */
    @PostMapping("/outbox")
    @Transactional
    public ResponseEntity<Object> publishTestEvent(@RequestBody(required = false) @jakarta.validation.Valid PublishTestEventRequest body) {
        String email = securityContextFacade.getEmail();
        if (email == null || !roleService.isSuperAdmin(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access denied: SUPER_ADMIN role required", "AUTH_003"));
        }

        String title = body != null ? body.getEffectiveTitle() : "Kafka Outbox Verification";
        String message = body != null ? body.getEffectiveMessage() : "Integration pipeline test successful.";
        Long userId = body != null ? body.getEffectiveUserId() : 1L;

        NotificationCreatedEvent payload = new NotificationCreatedEvent(
                userId,
                title,
                message,
                "SYSTEM",
                "HIGH"
        );

        EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                EventTypes.NOTIFICATION_CREATED,
                "Notification",
                String.valueOf(userId),
                payload
        );

        domainEventPublisher.publish(envelope);

        return ResponseEntity.ok(ApiResponse.success("Domain event published to transactional outbox", Map.of(
                "eventId", envelope.getEventId(),
                "eventType", envelope.getEventType(),
                "aggregateId", envelope.getAggregateId(),
                "correlationId", envelope.getCorrelationId()
        )));
    }
}
