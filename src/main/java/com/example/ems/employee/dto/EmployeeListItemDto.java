package com.example.ems.employee.dto;

public record EmployeeListItemDto(
    Long id,
    String employeeCode,
    String name,
    String designation,
    String department,
    String status,
    String workMode
) {}
