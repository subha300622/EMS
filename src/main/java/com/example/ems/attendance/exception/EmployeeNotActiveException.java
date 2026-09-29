package com.example.ems.attendance.exception;

public class EmployeeNotActiveException extends IllegalStateException {
    public EmployeeNotActiveException(String message) {
        super(message);
    }
}
