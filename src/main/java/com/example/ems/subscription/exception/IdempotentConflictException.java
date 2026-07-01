package com.example.ems.subscription.exception;

public class IdempotentConflictException extends RuntimeException {
    public IdempotentConflictException(String message) {
        super(message);
    }
}
