package com.example.ems.training.entity;

public enum TrainingStatus {
    DRAFT,
    PENDING_APPROVAL,
    REJECTED,
    CHANGES_REQUESTED,
    APPROVED,
    PUBLISHED,
    ONGOING,
    COMPLETED,
    CANCELLED,
    // Legacy compatibility
    ASSIGNED,
    IN_PROGRESS,
    CERTIFIED
}
