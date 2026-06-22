package com.example.ems.employee.dto;

import java.time.LocalDate;

public record EmployeeDocumentDto(
    Long id,
    String fileName,
    String fileType,
    String fileSize,
    String documentNumber,
    LocalDate issuedDate,
    LocalDate expiryDate,
    int version,
    String status,
    String verificationStatus,
    String documentTypeName,
    String documentTypeCode
) {}
