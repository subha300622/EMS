package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token refresh response")
public record TokenRefreshResponse(
    @Schema(description = "JWT Access token", example = "eyJhbGciOiJIUzI1NiIsIn...") String accessToken,
    @Schema(description = "JWT Refresh token", example = "eyJhbGciOiJIUzI1NiIsIn...") String refreshToken,
    @Schema(description = "Token type", example = "Bearer") String tokenType,
    @Schema(description = "Access token expiry in seconds", example = "900") int accessTokenExpiresIn,
    @Schema(description = "Refresh token expiry in seconds", example = "604800") int refreshTokenExpiresIn
) {}
