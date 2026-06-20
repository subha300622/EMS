package com.example.ems.settings.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class RegenerateBackupCodesRequest {

    @NotBlank(message = "Password is required")
    @Schema(example = "string")
    private String password;

    @NotBlank(message = "OTP is required")
    @Schema(example = "string")
    private String otp;

    public RegenerateBackupCodesRequest() {}

    public RegenerateBackupCodesRequest(String password, String otp) {
        this.password = password;
        this.otp = otp;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
