package com.example.ems.appraisal.service;

import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.common.exception.BadRequestException;

import java.util.*;

public class StateTransitionValidator {

    private static final Map<AppraisalStatus, Set<AppraisalStatus>> VALID_TRANSITIONS = new EnumMap<>(AppraisalStatus.class);

    static {
        // DRAFT -> SUBMITTED, MANAGER_APPROVED, DRAFT (self-updates)
        VALID_TRANSITIONS.put(AppraisalStatus.DRAFT, Set.of(AppraisalStatus.SUBMITTED, AppraisalStatus.MANAGER_APPROVED, AppraisalStatus.DRAFT));
        
        // ELIGIBLE -> SUBMITTED, DRAFT, ELIGIBLE
        VALID_TRANSITIONS.put(AppraisalStatus.ELIGIBLE, Set.of(AppraisalStatus.SUBMITTED, AppraisalStatus.DRAFT, AppraisalStatus.ELIGIBLE));

        // SUBMITTED -> UNDER_REVIEW, MANAGER_APPROVED, DRAFT, SUBMITTED
        VALID_TRANSITIONS.put(AppraisalStatus.SUBMITTED, Set.of(AppraisalStatus.UNDER_REVIEW, AppraisalStatus.MANAGER_APPROVED, AppraisalStatus.DRAFT, AppraisalStatus.SUBMITTED));

        // UNDER_REVIEW -> MANAGER_APPROVED, DRAFT, UNDER_REVIEW
        VALID_TRANSITIONS.put(AppraisalStatus.UNDER_REVIEW, Set.of(AppraisalStatus.MANAGER_APPROVED, AppraisalStatus.DRAFT, AppraisalStatus.UNDER_REVIEW));

        // MANAGER_APPROVED -> LOCKED, MANAGER_APPROVED
        VALID_TRANSITIONS.put(AppraisalStatus.MANAGER_APPROVED, Set.of(AppraisalStatus.LOCKED, AppraisalStatus.MANAGER_APPROVED));

        // LOCKED -> PENDING_FINANCE, LOCKED
        VALID_TRANSITIONS.put(AppraisalStatus.LOCKED, Set.of(AppraisalStatus.PENDING_FINANCE, AppraisalStatus.LOCKED));

        // PENDING_FINANCE -> FINANCE_APPROVED, FINANCE_REJECTED, UNDER_REVIEW, PENDING_FINANCE
        VALID_TRANSITIONS.put(AppraisalStatus.PENDING_FINANCE, Set.of(AppraisalStatus.FINANCE_APPROVED, AppraisalStatus.FINANCE_REJECTED, AppraisalStatus.UNDER_REVIEW, AppraisalStatus.PENDING_FINANCE));

        // FINANCE_APPROVED -> PROCESSED, FINANCE_APPROVED
        VALID_TRANSITIONS.put(AppraisalStatus.FINANCE_APPROVED, Set.of(AppraisalStatus.PROCESSED, AppraisalStatus.FINANCE_APPROVED));

        // PROCESSED -> CLOSED, PROCESSED
        VALID_TRANSITIONS.put(AppraisalStatus.PROCESSED, Set.of(AppraisalStatus.CLOSED, AppraisalStatus.PROCESSED));

        // CLOSED is terminal (fully immutable)
        VALID_TRANSITIONS.put(AppraisalStatus.CLOSED, Set.of(AppraisalStatus.CLOSED));
        
        // MANUAL_REVIEW_REQUIRED -> PENDING_FINANCE, DRAFT, MANUAL_REVIEW_REQUIRED
        VALID_TRANSITIONS.put(AppraisalStatus.MANUAL_REVIEW_REQUIRED, Set.of(AppraisalStatus.PENDING_FINANCE, AppraisalStatus.DRAFT, AppraisalStatus.MANUAL_REVIEW_REQUIRED));
        
        // FINANCE_REJECTED -> DRAFT, PENDING_FINANCE, FINANCE_REJECTED
        VALID_TRANSITIONS.put(AppraisalStatus.FINANCE_REJECTED, Set.of(AppraisalStatus.DRAFT, AppraisalStatus.PENDING_FINANCE, AppraisalStatus.FINANCE_REJECTED));
    }

    public static void validate(AppraisalStatus from, AppraisalStatus to) {
        if (from == AppraisalStatus.CLOSED) {
            throw new BadRequestException("CLOSED state is immutable.");
        }
        if (to == AppraisalStatus.CLOSED) {
            return;
        }
        if (from == AppraisalStatus.PROCESSED && to != AppraisalStatus.CLOSED && to != AppraisalStatus.PROCESSED) {
            throw new BadRequestException("PROCESSED state is write-locked (can only transition to CLOSED).");
        }
        if (from == AppraisalStatus.FINANCE_APPROVED && to != AppraisalStatus.PROCESSED && to != AppraisalStatus.FINANCE_APPROVED) {
            throw new BadRequestException("FINANCE_APPROVED state is read-only except for payroll execution (transition to PROCESSED).");
        }
        Set<AppraisalStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Collections.emptySet());
        if (!allowed.contains(to)) {
            throw new BadRequestException("Invalid state transition from " + from + " to " + to);
        }
    }
}
