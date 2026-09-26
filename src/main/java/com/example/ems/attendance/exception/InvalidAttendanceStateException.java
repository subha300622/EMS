package com.example.ems.attendance.exception;

public class InvalidAttendanceStateException extends IllegalStateException {
    public InvalidAttendanceStateException(String message) {
        super(message);
    }
}
