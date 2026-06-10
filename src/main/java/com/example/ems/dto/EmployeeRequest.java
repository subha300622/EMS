package com.example.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    private String email;

    private String employeeId;

    private String phone;

    private String gender;

    private LocalDate dob;

    private String address;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Annual salary is required")
    @Positive(message = "Annual salary must be positive")
    private BigDecimal annualSalary;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String location;

    private String employmentType;

    private String status;

    private Long managerId;
}
