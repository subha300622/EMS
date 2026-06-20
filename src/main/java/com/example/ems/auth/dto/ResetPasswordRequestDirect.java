package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequestDirect {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "OTP is required")
    @Schema(example = "string")
    private String otp;

    @NotBlank(message = "New password is required")
    @Schema(example = "string")
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
