package com.example.ems.common.util;

public class JobLevelIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Job Level ID");
        return String.format("JL-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Job Level ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("JL-")) {
            clean = clean.substring(3).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Job Level ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Job Level ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Job Level ID number format: " + str);
        }
    }
}
