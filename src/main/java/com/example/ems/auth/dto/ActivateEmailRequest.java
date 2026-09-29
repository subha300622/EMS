package com.example.ems.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ActivateEmailRequest {

    @NotBlank(message = "Email is required")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
