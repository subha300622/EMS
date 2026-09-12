package com.example.ems.attendance.exception;

public class DuplicateCheckInException extends IllegalArgumentException {
    public DuplicateCheckInException(String message) {
        super(message);
    }
}
