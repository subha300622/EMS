package com.example.ems.common.util;

public class DepartmentIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Department ID");
        return String.format("DEPT-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Department ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("DEPT-")) {
            clean = clean.substring(5).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Department ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Department ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Department ID number format: " + str);
        }
    }
}
