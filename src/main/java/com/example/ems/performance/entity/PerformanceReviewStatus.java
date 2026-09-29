package com.example.ems.performance.entity;

public enum PerformanceReviewStatus {
    DRAFT,
    SELF_REVIEW_PENDING,
    SELF_REVIEW_SUBMITTED,
    MANAGER_REVIEW_PENDING,
    MANAGER_REVIEW_SUBMITTED,
    CALCULATED,
    SUBMITTED,
    APPROVAL_PENDING,
    APPROVED,
    REJECTED,
    PUBLISHED,
    LOCKED,
    CANCELLED;

    public boolean isTerminal() {
        return this == LOCKED || this == CANCELLED;
    }

    public boolean isImmutable() {
        return this == PUBLISHED || this == LOCKED;
    }

    public static PerformanceReviewStatus fromString(String status) {
        if (status == null) return DRAFT;
        try {
            return PerformanceReviewStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown PerformanceReviewStatus: " + status);
        }
    }
}
