package com.example.ems.employee.dto;

public record EmployeeListItemDto(
    Long id,
    String employeeCode,
    Long organizationId,
    String name,
    String designation,
    String department,
    String status,
    String workMode,
    Long managerId,
    String managerName
) {}
