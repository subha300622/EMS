package com.example.ems.common.util;

public class EmploymentTypeIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Employment Type ID");
        return String.format("ET-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Employment Type ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("ET-")) {
            clean = clean.substring(3).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Employment Type ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Employment Type ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Employment Type ID number format: " + str);
        }
    }
}
