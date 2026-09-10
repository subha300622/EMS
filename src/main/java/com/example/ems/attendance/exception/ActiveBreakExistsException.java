package com.example.ems.attendance.exception;

public class ActiveBreakExistsException extends IllegalStateException {
    public ActiveBreakExistsException(String message) {
        super(message);
    }
}
