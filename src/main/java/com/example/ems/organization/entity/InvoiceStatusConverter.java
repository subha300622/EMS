package com.example.ems.organization.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InvoiceStatusConverter implements AttributeConverter<InvoiceStatus, String> {

    @Override
    public String convertToDatabaseColumn(InvoiceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public InvoiceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if ("PENDING".equalsIgnoreCase(dbData)) {
            return InvoiceStatus.ISSUED; // Map legacy PENDING to ISSUED
        }
        try {
            return InvoiceStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InvoiceStatus.ISSUED; // Fallback default
        }
    }
}
