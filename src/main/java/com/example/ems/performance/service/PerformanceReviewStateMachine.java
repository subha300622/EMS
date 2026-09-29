package com.example.ems.performance.service;

import com.example.ems.common.exception.ConflictException;
import com.example.ems.performance.entity.PerformanceReviewStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PerformanceReviewStateMachine {

    private static final Map<PerformanceReviewStatus, Set<PerformanceReviewStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PerformanceReviewStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.DRAFT, EnumSet.of(
                PerformanceReviewStatus.SELF_REVIEW_PENDING,
                PerformanceReviewStatus.SELF_REVIEW_SUBMITTED,
                PerformanceReviewStatus.MANAGER_REVIEW_PENDING,
                PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.SELF_REVIEW_PENDING, EnumSet.of(
                PerformanceReviewStatus.SELF_REVIEW_SUBMITTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.SELF_REVIEW_SUBMITTED, EnumSet.of(
                PerformanceReviewStatus.MANAGER_REVIEW_PENDING,
                PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.MANAGER_REVIEW_PENDING, EnumSet.of(
                PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED, EnumSet.of(
                PerformanceReviewStatus.CALCULATED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.CALCULATED, EnumSet.of(
                PerformanceReviewStatus.CALCULATED, // recalculate
                PerformanceReviewStatus.SUBMITTED,
                PerformanceReviewStatus.APPROVAL_PENDING,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.SUBMITTED, EnumSet.of(
                PerformanceReviewStatus.APPROVAL_PENDING,
                PerformanceReviewStatus.APPROVED,
                PerformanceReviewStatus.REJECTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.APPROVAL_PENDING, EnumSet.of(
                PerformanceReviewStatus.APPROVED,
                PerformanceReviewStatus.REJECTED,
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.REJECTED, EnumSet.of(
                PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED, // Reopen / correction path
                PerformanceReviewStatus.CANCELLED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.APPROVED, EnumSet.of(
                PerformanceReviewStatus.PUBLISHED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.PUBLISHED, EnumSet.of(
                PerformanceReviewStatus.LOCKED
        ));

        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.LOCKED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(PerformanceReviewStatus.CANCELLED, Collections.emptySet());
    }

    public void assertTransition(String currentStatusStr, PerformanceReviewStatus target, String operation) {
        PerformanceReviewStatus current = PerformanceReviewStatus.fromString(currentStatusStr);
        assertTransition(current, target, operation);
    }

    public void assertTransition(PerformanceReviewStatus current, PerformanceReviewStatus target, String operation) {
        if (current.isImmutable() && current == target) {
            throw new ConflictException("PERFORMANCE_LOCKED: Review is in status '" + current
                    + "' and is immutable. Operation '" + operation + "' is prohibited.");
        }

        if (current.isImmutable() && target != PerformanceReviewStatus.LOCKED) {
            throw new ConflictException("PERFORMANCE_LOCKED: Review is in status '" + current
                    + "' and cannot be modified or transitioned to '" + target + "' during operation '" + operation + "'.");
        }

        if (current == PerformanceReviewStatus.LOCKED || current == PerformanceReviewStatus.CANCELLED) {
            throw new ConflictException("PERFORMANCE_LOCKED: Review is in terminal status '" + current
                    + "' and cannot perform operation '" + operation + "'.");
        }

        Set<PerformanceReviewStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        if (!validTargets.contains(target)) {
            throw new IllegalStateException("Illegal state transition from '" + current
                    + "' to '" + target + "' during operation: " + operation);
        }
    }

    public void assertEditableState(String currentStatusStr, String operation) {
        PerformanceReviewStatus current = PerformanceReviewStatus.fromString(currentStatusStr);
        assertEditableState(current, operation);
    }

    public void assertEditableState(PerformanceReviewStatus current, String operation) {
        if (current.isImmutable()) {
            throw new ConflictException("PERFORMANCE_LOCKED: Review is in status '" + current
                    + "' and cannot be modified during operation '" + operation + "'.");
        }
        if (current == PerformanceReviewStatus.CANCELLED) {
            throw new IllegalStateException("Cannot " + operation + " review: Review is cancelled.");
        }
    }
}
