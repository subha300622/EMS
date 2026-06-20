package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    @NotBlank(message = "Refresh token is required")
    @Schema(example = "string")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
