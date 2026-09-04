package com.example.ems.common.util;

public class UserIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid User ID");
        return String.format("USR-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("USR-")) {
            clean = clean.substring(4).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid User ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("User ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid User ID number format: " + str);
        }
    }
}
