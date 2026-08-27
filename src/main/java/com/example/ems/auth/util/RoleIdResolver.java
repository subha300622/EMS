package com.example.ems.auth.util;

public class RoleIdResolver {

    public static String formatRoleId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for Role formatting");
        }
        return String.format("ROLE-%03d", id);
    }

    public static Long parseRoleId(String roleIdStr) {
        if (roleIdStr == null || roleIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Role ID cannot be blank");
        }
        String clean = roleIdStr.trim();
        if (clean.toUpperCase().startsWith("ROLE-")) {
            clean = clean.substring(5).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid roleId format: " + roleIdStr);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException("Role ID must be a positive integer: " + roleIdStr);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid roleId number format: " + roleIdStr);
        }
    }
}
