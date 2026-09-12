package com.example.ems.common.util;

public class OrganizationIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Organization ID");
        return String.format("ORG-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("ORG-")) {
            clean = clean.substring(4).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Organization ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Organization ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Organization ID number format: " + str);
        }
    }
}
