package com.example.ems.employee.domain;

import com.example.ems.employee.entity.EmployeeStatus;
import com.example.ems.employee.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(EmployeeLifecycleStateAndEventTest.TestLifecycleEventListener.class)
public class EmployeeLifecycleStateAndEventTest {

    @Autowired
    private EmployeeStatusTransitionValidator transitionValidator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TestLifecycleEventListener testEventListener;

    @BeforeEach
    void setUp() {
        testEventListener.clearEvents();
    }

    // ── Transition Validator Unit Tests ─────────────────────────────────────

    @Test
    @DisplayName("Valid status transitions succeed")
    void testValidTransitions() {
        // ONBOARDING -> PROBATION, ACTIVE
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ONBOARDING, EmployeeStatus.PROBATION));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ONBOARDING, EmployeeStatus.ACTIVE));

        // PROBATION -> ACTIVE, TERMINATED
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.PROBATION, EmployeeStatus.ACTIVE));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.PROBATION, EmployeeStatus.TERMINATED));

        // ACTIVE -> SUSPENDED, NOTICE_PERIOD, RETIRED, TERMINATED
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.SUSPENDED));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.RETIRED));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED));

        // SUSPENDED -> ACTIVE, TERMINATED
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.SUSPENDED, EmployeeStatus.ACTIVE));
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.SUSPENDED, EmployeeStatus.TERMINATED));

        // NOTICE_PERIOD -> TERMINATED
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.NOTICE_PERIOD, EmployeeStatus.TERMINATED));

        // RETIRED -> TERMINATED
        assertDoesNotThrow(() -> transitionValidator.validateTransition(EmployeeStatus.RETIRED, EmployeeStatus.TERMINATED));
    }

    @Test
    @DisplayName("Invalid status transitions are rejected with IllegalStateException")
    void testInvalidTransitions() {
        // ACTIVE -> ONBOARDING
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
                transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.ONBOARDING));
        assertTrue(ex1.getMessage().contains("Invalid status transition"));

        // RETIRED -> ACTIVE
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
                transitionValidator.validateTransition(EmployeeStatus.RETIRED, EmployeeStatus.ACTIVE));
        assertTrue(ex2.getMessage().contains("Invalid status transition"));

        // NOTICE_PERIOD -> ACTIVE
        assertThrows(IllegalStateException.class, () ->
                transitionValidator.validateTransition(EmployeeStatus.NOTICE_PERIOD, EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Terminal state protection: TERMINATED cannot transition to any other status")
    void testTerminalStateProtection() {
        for (EmployeeStatus target : EmployeeStatus.values()) {
            if (target != EmployeeStatus.TERMINATED) {
                assertThrows(IllegalStateException.class, () ->
                        transitionValidator.validateTransition(EmployeeStatus.TERMINATED, target));
                assertFalse(transitionValidator.isTransitionAllowed(EmployeeStatus.TERMINATED, target));
            }
        }
    }

    @Test
    @DisplayName("Same-state rejection: transitioning to identical status is rejected")
    void testSameStateRejection() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                transitionValidator.validateTransition(EmployeeStatus.ACTIVE, EmployeeStatus.ACTIVE));
        assertTrue(ex.getMessage().contains("already in ACTIVE status"));

        assertThrows(IllegalStateException.class, () ->
                transitionValidator.validateTransition(EmployeeStatus.TERMINATED, EmployeeStatus.TERMINATED));
    }

    @Test
    @DisplayName("EmployeeStatus enum normalization from string")
    void testEnumNormalization() {
        assertEquals(EmployeeStatus.ACTIVE, EmployeeStatus.fromString("ACTIVE"));
        assertEquals(EmployeeStatus.ACTIVE, EmployeeStatus.fromString("active"));
        assertEquals(EmployeeStatus.TERMINATED, EmployeeStatus.fromString("INACTIVE"));
        assertEquals(EmployeeStatus.TERMINATED, EmployeeStatus.fromString("EXITED"));
        assertThrows(IllegalArgumentException.class, () -> EmployeeStatus.fromString("UNKNOWN_STATUS"));
        assertThrows(IllegalArgumentException.class, () -> EmployeeStatus.fromString(null));
    }

    // ── Lifecycle Events Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Lifecycle events are immutable records containing correct IDs and timestamps")
    void testLifecycleEventDataIntegrity() {
        Instant now = Instant.now();

        EmployeeJoinedEvent joined = new EmployeeJoinedEvent(100L, 1L, now);
        assertEquals(100L, joined.employeeId());
        assertEquals(1L, joined.organizationId());
        assertEquals(now, joined.occurredAt());

        EmployeeActivatedEvent activated = new EmployeeActivatedEvent(100L, 1L, now);
        assertEquals(100L, activated.employeeId());

        EmployeeTransferredEvent transferred = new EmployeeTransferredEvent(100L, 1L, 10L, 20L, now);
        assertEquals(10L, transferred.fromDepartmentId());
        assertEquals(20L, transferred.toDepartmentId());

        EmployeeManagerChangedEvent managerChanged = new EmployeeManagerChangedEvent(100L, 1L, 5L, 8L, now);
        assertEquals(5L, managerChanged.oldManagerId());
        assertEquals(8L, managerChanged.newManagerId());

        EmployeeSuspendedEvent suspended = new EmployeeSuspendedEvent(100L, 1L, "Policy violation", now);
        assertEquals("Policy violation", suspended.reason());

        EmployeeTerminatedEvent terminated = new EmployeeTerminatedEvent(100L, 1L, "Resignation", now);
        assertEquals("Resignation", terminated.reason());
    }

    @Test
    @DisplayName("Transaction AFTER_COMMIT publishes event only upon successful commit")
    void testEventPublishesOnCommit() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeActivatedEvent(201L, 1L, Instant.now()));
            return null;
        });

        assertEquals(1, testEventListener.getActivatedEvents().size());
        assertEquals(201L, testEventListener.getActivatedEvents().get(0).employeeId());
    }

    @Test
    @DisplayName("Transaction rollback produces no lifecycle event in AFTER_COMMIT listener")
    void testEventSuppressedOnRollback() {
        try {
            transactionTemplate.execute(status -> {
                eventPublisher.publishEvent(new EmployeeActivatedEvent(999L, 1L, Instant.now()));
                throw new RuntimeException("Forced rollback");
            });
        } catch (RuntimeException ignored) {}

        // Listener must NOT have received the event because transaction rolled back
        assertTrue(testEventListener.getActivatedEvents().isEmpty());
    }

    // ── Test Event Listener Component ───────────────────────────────────────

    @Component
    public static class TestLifecycleEventListener {
        private final List<EmployeeActivatedEvent> activatedEvents = Collections.synchronizedList(new ArrayList<>());

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onActivated(EmployeeActivatedEvent event) {
            activatedEvents.add(event);
        }

        public List<EmployeeActivatedEvent> getActivatedEvents() {
            return activatedEvents;
        }

        public void clearEvents() {
            activatedEvents.clear();
        }
    }
}
