package com.example.ems.common.exception;

public class ConflictException extends RuntimeException {
    private final String errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = extractErrorCode(message);
    }

    public ConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : extractErrorCode(message);
    }

    public String getErrorCode() {
        return errorCode != null ? errorCode : "CONFLICT";
    }

    private static String extractErrorCode(String msg) {
        if (msg == null) return "CONFLICT";
        if (msg.startsWith("IDEMPOTENCY_KEY_REUSED:")) return "IDEMPOTENCY_KEY_REUSED";
        if (msg.startsWith("IDEMPOTENCY_IN_PROGRESS:")) return "IDEMPOTENCY_IN_PROGRESS";
        if (msg.startsWith("SNAPSHOT_TAMPERED:")) return "SNAPSHOT_TAMPERED";
        if (msg.startsWith("PERFORMANCE_LOCKED:")) return "PERFORMANCE_LOCKED";
        if (msg.contains(": ")) {
            int idx = msg.indexOf(": ");
            String potential = msg.substring(0, idx).trim();
            if (potential.matches("^[A-Z0-9_]+$")) {
                return potential;
            }
        }
        return "CONFLICT";
    }
}
