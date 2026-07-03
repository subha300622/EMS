package com.example.ems.attendance.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendanceStatusConverter implements AttributeConverter<AttendanceStatus, String> {

    @Override
    public String convertToDatabaseColumn(AttendanceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AttendanceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        String normalized = dbData.trim().toUpperCase().replace(" ", "_");
        if ("ON_LEAVE".equals(normalized)) {
            return AttendanceStatus.LEAVE;
        }
        try {
            return AttendanceStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return AttendanceStatus.PRESENT;
        }
    }
}
