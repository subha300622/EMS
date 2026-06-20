package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;

public class AcceptInvitationRequest {
    @NotBlank(message = "Invitation token is required")
    @Schema(example = "string")
    private String invitationToken;

    @NotBlank(message = "Password is required")
    @Schema(example = "string")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(example = "string")
    private String confirmPassword;

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
