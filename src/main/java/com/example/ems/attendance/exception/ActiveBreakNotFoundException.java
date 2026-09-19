package com.example.ems.attendance.exception;

public class ActiveBreakNotFoundException extends IllegalStateException {
    public ActiveBreakNotFoundException(String message) {
        super(message);
    }
}
