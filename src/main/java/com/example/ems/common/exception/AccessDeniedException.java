package com.example.ems.common.exception;

public class AccessDeniedException extends SecurityException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
