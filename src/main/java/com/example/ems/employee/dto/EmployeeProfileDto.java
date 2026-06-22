package com.example.ems.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeProfileDto(
    Long id,
    String employeeCode,
    String name,
    String designation,
    String department,
    String email,
    String phone,
    String location,
    String workMode,
    LocalDate joiningDate,
    BigDecimal currentCTC,
    String status
) {}
