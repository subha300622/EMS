package com.example.ems.employee.entity;

public enum EmployeeStatus {
    ONBOARDING,
    PROBATION,
    ACTIVE,
    SUSPENDED,
    NOTICE_PERIOD,
    TERMINATED,
    RETIRED;

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        for (EmployeeStatus s : values()) {
            if (s.name().equalsIgnoreCase(status.trim())) {
                return true;
            }
        }
        return false;
    }

    public static EmployeeStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Employee status cannot be null or blank");
        }
        String normalized = status.trim().toUpperCase();
        if ("INACTIVE".equals(normalized) || "EXITED".equals(normalized)) {
            return TERMINATED;
        }
        try {
            return EmployeeStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown employee status: " + status);
        }
    }
}
