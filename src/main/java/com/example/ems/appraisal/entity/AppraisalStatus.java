package com.example.ems.appraisal.entity;

public enum AppraisalStatus {
    CREATED,
    SELF_ASSESSMENT,
    STAGE_REVIEW,
    FINAL_REVIEW,
    COMPLETED,
    PUBLISHED,
    CANCELLED,
    // Legacy support values
    DRAFT,
    SUBMITTED,
    ELIGIBLE,
    MANUAL_REVIEW_REQUIRED,
    PENDING_FINANCE,
    FINANCE_APPROVED,
    FINANCE_REJECTED,
    PROCESSED,
    UNDER_REVIEW,
    MANAGER_APPROVED,
    LOCKED,
    FINANCE_PENDING,
    CLOSED
}
