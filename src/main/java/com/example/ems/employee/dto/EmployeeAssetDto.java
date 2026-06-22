package com.example.ems.employee.dto;

import java.time.LocalDate;

public record EmployeeAssetDto(
    Long id,
    String assetCode,
    String assetName,
    String category,
    String brand,
    String model,
    String serialNumber,
    String status,
    String condition,
    LocalDate assignedDate
) {}
