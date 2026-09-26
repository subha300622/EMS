package com.example.ems.performance.entity;

public enum KpiMeasurementType {
    HIGHER_IS_BETTER,
    LOWER_IS_BETTER,
    PERCENTAGE,
    BOOLEAN,
    RATING_BASED;

    public static KpiMeasurementType fromString(String type) {
        if (type == null) return HIGHER_IS_BETTER;
        try {
            return KpiMeasurementType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown KpiMeasurementType: " + type);
        }
    }
}
