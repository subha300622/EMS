package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailPayslipRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(example = "john.doe@example.com")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
