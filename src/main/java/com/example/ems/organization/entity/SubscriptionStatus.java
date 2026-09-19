package com.example.ems.organization.entity;

public enum SubscriptionStatus {
    TRIAL,
    ACTIVE,
    EXPIRED,
    CANCELLED,
    SUSPENDED,
    PENDING, // Draft / pending payment legacy
    ARCHIVED,
    INCOMPLETE,
    PENDING_PAYMENT,
    PAST_DUE
}
