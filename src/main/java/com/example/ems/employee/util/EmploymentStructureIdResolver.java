package com.example.ems.employee.util;

public class EmploymentStructureIdResolver {

    public static String formatDesignationId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Designation ID");
        return String.format("DES-%03d", id);
    }

    public static String formatJobLevelId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Job Level ID");
        return String.format("JL-%03d", id);
    }

    public static String formatEmploymentTypeId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Employment Type ID");
        return String.format("ET-%03d", id);
    }

    public static Long parseDesignationId(String str) {
        return parseWithPrefix(str, "DES-", "Designation");
    }

    public static Long parseJobLevelId(String str) {
        return parseWithPrefix(str, "JL-", "Job Level");
    }

    public static Long parseEmploymentTypeId(String str) {
        return parseWithPrefix(str, "ET-", "Employment Type");
    }

    private static Long parseWithPrefix(String str, String prefix, String typeName) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(typeName + " ID cannot be blank");
        }
        String clean = str.trim();
        if (clean.toUpperCase().startsWith(prefix)) {
            clean = clean.substring(prefix.length()).trim();
        }
        if (clean.isEmpty() || !clean.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid " + typeName + " ID format: " + str);
        }
        try {
            Long parsedId = Long.parseLong(clean);
            if (parsedId <= 0) {
                throw new IllegalArgumentException(typeName + " ID must be a positive integer: " + str);
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + typeName + " ID number format: " + str);
        }
    }
}
