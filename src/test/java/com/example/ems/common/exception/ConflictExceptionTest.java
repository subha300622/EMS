package com.example.ems.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConflictExceptionTest {

    @Test
    @DisplayName("Should extract known prefix error codes")
    void shouldExtractKnownPrefixErrorCodes() {
        ConflictException ex1 = new ConflictException("IDEMPOTENCY_KEY_REUSED: Key was reused");
        assertEquals("IDEMPOTENCY_KEY_REUSED", ex1.getErrorCode());
        assertEquals("IDEMPOTENCY_KEY_REUSED: Key was reused", ex1.getMessage());

        ConflictException ex2 = new ConflictException("IDEMPOTENCY_IN_PROGRESS: Request pending");
        assertEquals("IDEMPOTENCY_IN_PROGRESS", ex2.getErrorCode());

        ConflictException ex3 = new ConflictException("SNAPSHOT_TAMPERED: Digest mismatch");
        assertEquals("SNAPSHOT_TAMPERED", ex3.getErrorCode());

        ConflictException ex4 = new ConflictException("PERFORMANCE_LOCKED: Review is locked");
        assertEquals("PERFORMANCE_LOCKED", ex4.getErrorCode());
    }

    @Test
    @DisplayName("Should extract arbitrary uppercase error codes with colon prefix")
    void shouldExtractArbitraryPrefixErrorCode() {
        ConflictException ex = new ConflictException("CYCLE_NOT_ACTIVE: The cycle is not active");
        assertEquals("CYCLE_NOT_ACTIVE", ex.getErrorCode());
        assertEquals("CYCLE_NOT_ACTIVE: The cycle is not active", ex.getMessage());
    }

    @Test
    @DisplayName("Should fallback to CONFLICT when no pattern matches")
    void shouldFallbackToDefaultErrorCode() {
        ConflictException ex1 = new ConflictException("Standard conflict error message without prefix");
        assertEquals("CONFLICT", ex1.getErrorCode());

        ConflictException ex2 = new ConflictException(null);
        assertEquals("CONFLICT", ex2.getErrorCode());
        assertNull(ex2.getMessage());
    }

    @Test
    @DisplayName("Explicit error code should take precedence")
    void explicitErrorCodePrecedence() {
        ConflictException ex = new ConflictException("Some conflict", "CUSTOM_CODE");
        assertEquals("CUSTOM_CODE", ex.getErrorCode());
        assertEquals("Some conflict", ex.getMessage());
    }
}
