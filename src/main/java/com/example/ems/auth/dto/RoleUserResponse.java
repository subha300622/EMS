package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class RoleUserResponse {

    @Schema(example = "string")
    private String userId;
    @Schema(example = "John Doe")
    private String fullName;
    @Schema(example = "john.doe@example.com")
    private String email;
    @Schema(example = "ACTIVE")
    private String status;

    public RoleUserResponse() {}

    public RoleUserResponse(String userId, String fullName, String email, String status) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
