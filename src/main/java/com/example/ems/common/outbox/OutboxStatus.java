package com.example.ems.common.outbox;

/**
 * Status values for outbox event lifecycle.
 */
public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    FAILED_PERMANENT
}
