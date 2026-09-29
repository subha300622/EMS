package com.example.ems.common.util;

public class RoleIdResolver {

    public static String formatId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Role ID");
        return String.format("ROLE-%03d", id);
    }

    public static Long parseId(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Role ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith("ROLE-")) {
            clean = clean.substring(5).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Role ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Role ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Role ID number format: " + str);
        }
    }

    // Deprecated/Compatibility helpers
    public static String formatRoleId(Long id) { return formatId(id); }
    public static Long parseRoleId(String str) { return parseId(str); }
}
