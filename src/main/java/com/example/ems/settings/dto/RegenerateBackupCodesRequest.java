package com.example.ems.settings.dto;

import jakarta.validation.constraints.NotBlank;

public class RegenerateBackupCodesRequest {

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "OTP is required")
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
