package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;

public class PlatformMessageRequest {

    @NotBlank(message = "Message text is required")
    private String message;

    private boolean isInternal = false;

    public PlatformMessageRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
}
