package com.example.ems.payroll.entity;

public enum PayrollRunStatus {
    DRAFT,
    CALCULATING,
    PROCESSING,
    CALCULATED,
    PENDING_APPROVAL,
    APPROVED,
    LOCKED,
    FINALIZED,
    PAYMENT_PROCESSING,
    PAID,
    REJECTED,
    CHANGES_REQUESTED,
    FAILED
}
